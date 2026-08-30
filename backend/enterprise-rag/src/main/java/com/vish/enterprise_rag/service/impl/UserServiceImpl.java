package com.vish.enterprise_rag.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vish.enterprise_rag.entities.Document;
import com.vish.enterprise_rag.entities.DocumentOwnershipHistory;
import com.vish.enterprise_rag.entities.User;
import com.vish.enterprise_rag.enums.UserActionType;
import com.vish.enterprise_rag.mappers.UserMapper;
import com.vish.enterprise_rag.repositories.read.DocumentReadRepository;
import com.vish.enterprise_rag.repositories.read.OrganizationReadRepository;
import com.vish.enterprise_rag.repositories.read.UserReadRepository;
import com.vish.enterprise_rag.repositories.write.DocumentOwnershipHistoryWriteRepository;
import com.vish.enterprise_rag.repositories.write.DocumentWriteRepository;
import com.vish.enterprise_rag.repositories.write.UserWriteRepository;
import com.vish.enterprise_rag.requests.UserReq;
import com.vish.enterprise_rag.response.ResponseDTO;
import com.vish.enterprise_rag.security.SecurityUtils;
import com.vish.enterprise_rag.service.AuditService;
import com.vish.enterprise_rag.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserReadRepository userReadRepository;
    private final UserWriteRepository userWriteRepository;
    private final UserMapper userMapper;
    private final OrganizationReadRepository organizationReadRepository;
    private final DocumentReadRepository documentReadRepository;
    private final DocumentWriteRepository documentWriteRepository;
    private final DocumentOwnershipHistoryWriteRepository documentOwnershipHistoryWriteRepository;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public ResponseEntity<?> createUser(UserReq request) {
        log.info("Creating user: {}", request);
        try {
            boolean hasEmail = request.getEmail() != null && !request.getEmail().trim().isEmpty();

            if (!hasEmail) {
                return ResponseEntity.ok(ResponseDTO.error("Email is required"));
            }

            if (userReadRepository.findByEmailAndIsActiveTrue(request.getEmail()).isPresent()) {
                return ResponseEntity.ok(ResponseDTO.error("User with this email already exists"));
            }

            if (request.getOrganizationId() == null) {
                Long currentOrgId = SecurityUtils.getCurrentOrganizationId();
                if (currentOrgId != null) {
                    request.setOrganizationId(currentOrgId);
                } else {
                    return ResponseEntity.ok(ResponseDTO.error("Organization ID is required"));
                }
            }

            User user = userMapper.toEntity(request);
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user = userWriteRepository.save(user);
            auditService.audit(UserActionType.CREATE_USER, user, "Created new user: " + user.getEmail());
            return ResponseEntity.ok(ResponseDTO.success("User created successfully", userMapper.toRes(user)));
        } catch (Exception e) {
            log.error("Exception occurred while creating user", e);
            return ResponseEntity.ok(ResponseDTO.error(e.getMessage()));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> updateUser(long id, UserReq request) {
        log.info("Updating user: {} with ID: {}", request, id);
        try {
            Optional<User> existingUser = userReadRepository.findByIdAndIsActiveTrue(id);
            if (existingUser.isEmpty()) {
                return ResponseEntity.ok(ResponseDTO.error("User not found with ID: " + id));
            }
            User user = existingUser.get();

            Long currentOrgId = SecurityUtils.getCurrentOrganizationId();
            if (currentOrgId != null && !user.getOrganization().getId().equals(currentOrgId) && !SecurityUtils.isAdmin()) {
                return ResponseEntity.ok(ResponseDTO.error("You do not have permission to modify users outside your organization"));
            }

            if (request.getName() != null && !request.getName().trim().isEmpty()) {
                user.setName(request.getName());
            }
            if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                user.setEmail(request.getEmail());
            }
            if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
            }
            if (request.getDesignation() != null && !request.getDesignation().trim().isEmpty()) {
                user.setDesignation(request.getDesignation());
            }
            if (request.getOrganizationId() != null) {
                var org = organizationReadRepository.findByIdAndIsActiveTrue(request.getOrganizationId());
                if (org.isEmpty()) {
                    return ResponseEntity.ok(ResponseDTO.error("Organization not found with ID: " + request.getOrganizationId()));
                }
                user.setOrganization(org.get());
            }
            userWriteRepository.save(user);
            auditService.audit(UserActionType.EDIT_USER, user, "Updated user: " + user.getEmail());
            return ResponseEntity.ok(ResponseDTO.success("User updated successfully", userMapper.toRes(user)));
        } catch (Exception e) {
            log.error("Exception occurred while updating user with ID {}", id, e);
            return ResponseEntity.ok(ResponseDTO.error(e.getMessage()));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> deleteUser(long id) {
        log.info("Deactivating user with ID: {}", id);
        try {
            Optional<User> existingUser = userReadRepository.findByIdAndIsActiveTrue(id);
            if (existingUser.isEmpty()) {
                return ResponseEntity.ok(ResponseDTO.error("User not found with ID: " + id));
            }
            User user = existingUser.get();

            Long currentOrgId = SecurityUtils.getCurrentOrganizationId();
            if (currentOrgId != null && !user.getOrganization().getId().equals(currentOrgId) && !SecurityUtils.isAdmin()) {
                return ResponseEntity.ok(ResponseDTO.error("You do not have permission to deactivate users outside your organization"));
            }

            // Core domain rule: When owner is deactivated, reassign active document ownership & audit
            List<Document> ownedDocuments = documentReadRepository.findByOwnerIdAndIsActiveTrue(id);
            if (!ownedDocuments.isEmpty()) {
                Long actingUserId = SecurityUtils.getCurrentUserId();
                User newOwner = (actingUserId != null && !actingUserId.equals(id))
                        ? userReadRepository.findByIdAndIsActiveTrue(actingUserId).orElse(null)
                        : null;

                for (Document doc : ownedDocuments) {
                    if (newOwner != null) {
                        DocumentOwnershipHistory history = new DocumentOwnershipHistory();
                        history.setDocument(doc);
                        history.setOldUser(user);
                        history.setNewUser(newOwner);
                        history.setOwnershipChangedAt(LocalDateTime.now());
                        documentOwnershipHistoryWriteRepository.save(history);

                        doc.setOwner(newOwner);
                        documentWriteRepository.save(doc);

                        auditService.audit(UserActionType.DOCUMENT_PERMISSION_UPDATE, doc,
                                String.format("Reassigned document ownership from user %d to user %d upon deactivation", id, newOwner.getId()));
                    }
                }
            }

            user.setIsActive(false);
            userWriteRepository.save(user);
            auditService.audit(UserActionType.DELETE_USER, user, "Deactivated user: " + user.getEmail());
            return ResponseEntity.ok(ResponseDTO.success("User deactivated successfully", null));
        } catch (Exception e) {
            log.error("Exception occurred while deleting user with ID {}", id, e);
            return ResponseEntity.ok(ResponseDTO.error("Error occurred while processing user deletion"));
        }
    }

    @Override
    public ResponseEntity<?> getAllUsers() {
        log.info("Getting users for current organization");
        try {
            Long currentOrgId = SecurityUtils.getCurrentOrganizationId();
            List<User> users;
            if (currentOrgId != null) {
                users = userReadRepository.findByOrganizationIdAndIsActiveTrue(currentOrgId);
            } else {
                users = userReadRepository.findByIsActiveTrue();
            }
            return ResponseEntity.ok(ResponseDTO.success("Users fetched successfully", users.stream().map(userMapper::toRes).toList()));
        } catch (Exception e) {
            log.error("Exception occurred while getting all users", e);
            return ResponseEntity.ok(ResponseDTO.error("Error occurred while processing user retrieval"));
        }
    }

    @Override
    public ResponseEntity<?> getUser(long id) {
        log.info("Getting user with ID: {}", id);
        try {
            Optional<User> existingUser = userReadRepository.findByIdAndIsActiveTrue(id);
            if (existingUser.isEmpty()) {
                return ResponseEntity.ok(ResponseDTO.error("User not found with ID: " + id));
            }
            User user = existingUser.get();
            Long currentOrgId = SecurityUtils.getCurrentOrganizationId();
            if (currentOrgId != null && !user.getOrganization().getId().equals(currentOrgId) && !SecurityUtils.isAdmin()) {
                return ResponseEntity.ok(ResponseDTO.error("User not found in your organization"));
            }
            return ResponseEntity.ok(ResponseDTO.success("User found successfully", userMapper.toRes(user)));
        } catch (Exception e) {
            log.error("Exception occurred while getting user with ID {}", id, e);
            return ResponseEntity.ok(ResponseDTO.error("Error occurred while processing user retrieval"));
        }
    }
}
