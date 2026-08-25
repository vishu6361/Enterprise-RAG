package com.vish.enterprise_rag.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.vish.enterprise_rag.entities.Document;
import com.vish.enterprise_rag.entities.DocumentPermission;
import com.vish.enterprise_rag.entities.Organization;
import com.vish.enterprise_rag.entities.User;
import com.vish.enterprise_rag.enums.DocumentPermissionType;
import com.vish.enterprise_rag.enums.DocumentStatus;
import com.vish.enterprise_rag.enums.UserActionType;
import com.vish.enterprise_rag.mappers.DocumentMapper;
import com.vish.enterprise_rag.repositories.read.DocumentPermissionReadRepository;
import com.vish.enterprise_rag.repositories.read.DocumentReadRepository;
import com.vish.enterprise_rag.repositories.read.OrganizationReadRepository;
import com.vish.enterprise_rag.repositories.read.UserReadRepository;
import com.vish.enterprise_rag.repositories.write.DocumentPermissionWriteRepository;
import com.vish.enterprise_rag.repositories.write.DocumentWriteRepository;
import com.vish.enterprise_rag.requests.DocumentPermissionUpdateReq;
import com.vish.enterprise_rag.response.DocumentRes;
import com.vish.enterprise_rag.response.ResponseDTO;
import com.vish.enterprise_rag.security.SecurityUtils;
import com.vish.enterprise_rag.service.AuditService;
import com.vish.enterprise_rag.service.DocumentService;
import com.vish.enterprise_rag.utils.CommonUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentReadRepository documentReadRepository;
    private final OrganizationReadRepository organizationReadRepository;
    private final UserReadRepository userReadRepository;
    private final DocumentWriteRepository documentWriteRepository;
    private final DocumentPermissionWriteRepository documentPermissionWriteRepository;
    private final DocumentPermissionReadRepository documentPermissionReadRepository;
    private final AuditService auditService;
    private final DocumentMapper documentMapper;

    @Override
    @Transactional
    public ResponseEntity<?> uploadDocument(MultipartFile file) {
        log.info("Document upload initiated for file: {}, size: {} bytes", file.getOriginalFilename(), file.getSize());
        try {
            Long orgId = SecurityUtils.getCurrentOrganizationId();
            Long userId = SecurityUtils.getCurrentUserId();
            if (orgId == null || userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseDTO.error("User not authenticated"));
            }

            String contentHash = CommonUtils.calculateSHA256(file.getBytes());

            if (documentReadRepository.findByOrganizationIdAndContentHashAndIsActiveTrue(orgId, contentHash).isPresent()) {
                log.info("Document already exists with SHA-256 content hash for file: {}", file.getOriginalFilename());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(ResponseDTO.error("Document already exists in this organization"));
            }

            User user = userReadRepository.findByIdAndIsActiveTrue(userId)
                    .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
            Organization organization = organizationReadRepository.findByIdAndIsActiveTrue(orgId)
                    .orElseThrow(() -> new IllegalStateException("Authenticated organization not found"));

            Document document = new Document();
            document.setOrganization(organization);
            document.setContentHash(contentHash);
            document.setExtension(file.getContentType());
            document.setIsActive(true);
            document.setDocumentName(file.getOriginalFilename());
            document.setOwner(user);
            document.setStatus(DocumentStatus.PROCESSING);
            document = documentWriteRepository.save(document);

            DocumentPermission documentPermission = new DocumentPermission();
            documentPermission.setDocument(document);
            documentPermission.setUser(user);
            documentPermission.setPermission(DocumentPermissionType.OWNER);
            documentPermissionWriteRepository.save(documentPermission);

            auditService.audit(UserActionType.UPLOAD_DOCUMENT, document, "Uploaded document: " + file.getOriginalFilename());
            return ResponseEntity.ok(ResponseDTO.success("Document uploaded successfully", documentMapper.toRes(document)));
        } catch (Exception e) {
            log.error("Exception occurred in file upload process", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseDTO.error("Error occurred in file upload process"));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getDocuments() {
        Long orgId = SecurityUtils.getCurrentOrganizationId();
        if (orgId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseDTO.error("User not authenticated"));
        }
        List<Document> documents = documentReadRepository.findByOrganizationIdAndIsActiveTrue(orgId);
        List<DocumentRes> result = documents.stream().map(documentMapper::toRes).toList();
        return ResponseEntity.ok(ResponseDTO.success("Documents fetched successfully", result));
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getDocument(Long id) {
        Long orgId = SecurityUtils.getCurrentOrganizationId();
        if (orgId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseDTO.error("User not authenticated"));
        }
        Optional<Document> documentOpt = documentReadRepository.findByIdAndIsActiveTrue(id);
        if (documentOpt.isEmpty() || !documentOpt.get().getOrganization().getId().equals(orgId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseDTO.error("Document not found"));
        }
        return ResponseEntity.ok(ResponseDTO.success("Document fetched successfully", documentMapper.toRes(documentOpt.get())));
    }

    @Override
    @Transactional
    public ResponseEntity<?> deleteDocument(Long id) {
        log.info("Deleting document with ID: {}", id);
        try {
            Long orgId = SecurityUtils.getCurrentOrganizationId();
            Long userId = SecurityUtils.getCurrentUserId();
            if (orgId == null || userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseDTO.error("User not authenticated"));
            }

            Optional<Document> documentOpt = documentReadRepository.findByIdAndIsActiveTrue(id);
            if (documentOpt.isEmpty() || !documentOpt.get().getOrganization().getId().equals(orgId)) {
                log.info("Document not found with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseDTO.error("Document not found"));
            }

            Document document = documentOpt.get();
            boolean isOwner = document.getOwner().getId().equals(userId);
            boolean isAdmin = SecurityUtils.isAdmin();
            boolean hasOwnerPermission = documentPermissionReadRepository
                    .existsByDocumentIdAndUserIdAndPermissionAndIsActiveTrue(id, userId, DocumentPermissionType.OWNER);

            if (!isOwner && !isAdmin && !hasOwnerPermission) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseDTO.error("You do not have permission to delete this document"));
            }

            document.setIsActive(false);
            documentWriteRepository.save(document);

            List<DocumentPermission> documentPermissions = documentPermissionReadRepository.findByDocumentIdAndIsActiveTrue(id);
            if (!documentPermissions.isEmpty()) {
                documentPermissions.forEach(permission -> {
                    permission.setIsActive(false);
                    documentPermissionWriteRepository.save(permission);
                });
            }

            auditService.audit(UserActionType.DELETE_DOCUMENT, document, "Deleted document with ID: " + id);
            return ResponseEntity.ok(ResponseDTO.success("Document deleted successfully"));
        } catch (Exception e) {
            log.error("Exception occurred in file deletion process", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseDTO.error("Error occurred in file deletion process"));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> updatePermission(DocumentPermissionUpdateReq request) {
        log.info("Document permission update initiated for body: {}", request);
        try {
            Long orgId = SecurityUtils.getCurrentOrganizationId();
            Long userId = SecurityUtils.getCurrentUserId();
            if (orgId == null || userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseDTO.error("User not authenticated"));
            }

            Optional<Document> documentOpt = documentReadRepository.findByIdAndIsActiveTrue(request.getDocumentId());
            if (documentOpt.isEmpty() || !documentOpt.get().getOrganization().getId().equals(orgId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseDTO.error("Document not found"));
            }
            Document document = documentOpt.get();

            boolean isOwner = document.getOwner().getId().equals(userId);
            boolean isAdmin = SecurityUtils.isAdmin();
            if (!isOwner && !isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseDTO.error("Only document owner or admin can update permissions"));
            }

            Optional<User> targetUserOpt = userReadRepository.findByIdAndIsActiveTrue(request.getUserId());
            if (targetUserOpt.isEmpty() || !targetUserOpt.get().getOrganization().getId().equals(orgId)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseDTO.error("Target user does not belong to your organization"));
            }

            Optional<DocumentPermission> documentPermission = documentPermissionReadRepository
                    .findByDocumentIdAndUserIdAndIsActiveTrue(request.getDocumentId(), request.getUserId());

            if (documentPermission.isEmpty()) {
                log.info("Document permission not found with document ID: {}, user ID: {}", request.getDocumentId(), request.getUserId());
                DocumentPermission newPermission = new DocumentPermission();
                newPermission.setDocument(document);
                newPermission.setUser(targetUserOpt.get());
                newPermission.setPermission(request.getPermissionType());
                documentPermissionWriteRepository.save(newPermission);
                auditService.audit(UserActionType.DOCUMENT_PERMISSION_ADD, newPermission,
                        String.format("Document permission added for document ID: %d and user ID: %d", request.getDocumentId(), request.getUserId()));
                return ResponseEntity.ok(ResponseDTO.success("Document permission added successfully"));
            } else {
                log.info("Document permission found with document ID: {}, user ID: {}", request.getDocumentId(), request.getUserId());
                DocumentPermission existingPerm = documentPermission.get();
                existingPerm.setPermission(request.getPermissionType());
                documentPermissionWriteRepository.save(existingPerm);
                auditService.audit(UserActionType.DOCUMENT_PERMISSION_UPDATE, existingPerm,
                        String.format("Document permission updated for document ID: %d and user ID: %d", request.getDocumentId(), request.getUserId()));
                return ResponseEntity.ok(ResponseDTO.success("Document permission updated successfully"));
            }
        } catch (Exception e) {
            log.error("Exception occurred in document permission update process", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseDTO.error("Error occurred in document permission update process"));
        }
    }
}
