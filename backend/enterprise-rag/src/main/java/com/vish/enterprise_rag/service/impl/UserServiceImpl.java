package com.vish.enterprise_rag.service.impl;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.vish.enterprise_rag.entities.User;
import com.vish.enterprise_rag.mappers.UserMapper;
import com.vish.enterprise_rag.repositories.read.OrganizationReadRepository;
import com.vish.enterprise_rag.repositories.read.UserReadRepository;
import com.vish.enterprise_rag.repositories.write.UserWriteRepository;
import com.vish.enterprise_rag.requests.UserReq;
import com.vish.enterprise_rag.response.ResponseDTO;
import com.vish.enterprise_rag.service.UserService;

import jakarta.transaction.Transactional;
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
            User user = userMapper.toEntity(request);
            user = userWriteRepository.save(user);
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
            if (request.getName() != null && !request.getName().trim().isEmpty()) {
                user.setName(request.getName());
            }
            if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                user.setEmail(request.getEmail());
            }
            if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
                user.setPassword(request.getPassword());
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
            return ResponseEntity.ok(ResponseDTO.success("User updated successfully", userMapper.toRes(user)));
        } catch (Exception e) {
            log.error("Exception occurred while updating user with ID {}", id, e);
            return ResponseEntity.ok(ResponseDTO.error(e.getMessage()));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> deleteUser(long id) {
        log.info("Deleting user with ID: {}", id);
        try {
            Optional<User> existingUser = userReadRepository.findByIdAndIsActiveTrue(id);
            if (existingUser.isEmpty()) {
                return ResponseEntity.ok(ResponseDTO.error("User not found with ID: " + id));
            }
            User user = existingUser.get();
            user.setIsActive(false);
            userWriteRepository.save(user);
            return ResponseEntity.ok(ResponseDTO.success("User deleted successfully", null));
        } catch (Exception e) {
            log.error("Exception occurred while deleting user with ID {}", id, e);
            return ResponseEntity.ok(ResponseDTO.error("Error occurred while processing user deletion"));
        }
    }

    @Override
    public ResponseEntity<?> getAllUsers() {
        log.info("Getting all users");
        try {
            return ResponseEntity.ok(ResponseDTO.success("Users fetched successfully", userReadRepository.findByIsActiveTrue().stream().map(userMapper::toRes).toList()));
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
            return ResponseEntity.ok(ResponseDTO.success("User found successfully", userMapper.toRes(existingUser.get())));
        } catch (Exception e) {
            log.error("Exception occurred while getting user with ID {}", id, e);
            return ResponseEntity.ok(ResponseDTO.error("Error occurred while processing user retrieval"));
        }
    }
}
