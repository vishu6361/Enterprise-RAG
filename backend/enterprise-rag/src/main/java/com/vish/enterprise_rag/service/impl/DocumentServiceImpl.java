package com.vish.enterprise_rag.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.vish.enterprise_rag.entities.Document;
import com.vish.enterprise_rag.entities.DocumentPermission;
import com.vish.enterprise_rag.entities.Organization;
import com.vish.enterprise_rag.entities.User;
import com.vish.enterprise_rag.enums.DocumentPermissionType;
import com.vish.enterprise_rag.enums.DocumentStatus;
import com.vish.enterprise_rag.enums.UserActionType;
import com.vish.enterprise_rag.repositories.read.DocumentPermissionReadRepository;
import com.vish.enterprise_rag.repositories.read.DocumentReadRepository;
import com.vish.enterprise_rag.repositories.read.OrganizationReadRepository;
import com.vish.enterprise_rag.repositories.read.UserReadRepository;
import com.vish.enterprise_rag.repositories.write.DocumentPermissionWriteRepository;
import com.vish.enterprise_rag.repositories.write.DocumentWriteRepository;
import com.vish.enterprise_rag.requests.DocumentPermissionUpdateReq;
import com.vish.enterprise_rag.response.ResponseDTO;
import com.vish.enterprise_rag.service.AuditService;
import com.vish.enterprise_rag.service.DocumentService;
import com.vish.enterprise_rag.utils.CommonUtils;

import jakarta.transaction.Transactional;
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

    @Override
    @Transactional
    public ResponseEntity<?> uploadDocument(MultipartFile file) {
        log.info("Document upload initiated for file: {}, size: {} bytes", file.getOriginalFilename(), file.getSize());
        try {
            String contentHash = CommonUtils.calculateSHA256(file.getBytes());
            Long testOrgId = 1L;
            Long testUserId = 1L;
            if (documentReadRepository.findByOrganizationIdAndContentHashAndIsActiveTrue(testOrgId, contentHash).isPresent()) {
                log.info("Document already exists with SHA-256 content hash for file: {}", file.getOriginalFilename());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(ResponseDTO.error("Document already exists"));
            }
            User testUser = userReadRepository.findByIdAndIsActiveTrue(testUserId).get();
            Organization testOrganization = organizationReadRepository.findByIdAndIsActiveTrue(testOrgId).get();

            Document document = new Document();
            document.setOrganization(testOrganization);
            document.setContentHash(contentHash);
            document.setExtension(file.getContentType());
            document.setIsActive(true);
            document.setDocumentName(file.getOriginalFilename());
            document.setOwner(testUser);
            document.setStatus(DocumentStatus.PROCESSING);
            document = documentWriteRepository.save(document);
            DocumentPermission documentPermission = new DocumentPermission();
            documentPermission.setDocument(document);
            documentPermission.setUser(testUser);
            documentPermission.setPermission(DocumentPermissionType.OWNER);
            documentPermissionWriteRepository.save(documentPermission);
            return ResponseEntity.ok(ResponseDTO.success("Document uploaded successfully"));
        } catch (Exception e) {
            log.error("Exception occurred in file upload process", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseDTO.error("Error occurred in file upload process"));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> deleteDocument(Long id) {
        log.info("Deleting document with ID: {}", id);
        try {
            Optional<Document> document = documentReadRepository.findByIdAndIsActiveTrue(id);
            if (document.isEmpty()) {
                log.info("Document not found with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseDTO.error("Document not found"));
            }
            document.get().setIsActive(false);
            documentWriteRepository.save(document.get());
            List<DocumentPermission> documentPermission = documentPermissionReadRepository.findByDocumentIdAndIsActiveTrue(id);
            if (!documentPermission.isEmpty()) {
                documentPermission.forEach(permission -> {
                    permission.setIsActive(false);
                    documentPermissionWriteRepository.save(permission);
                });
            }
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
            Optional<DocumentPermission> documentPermission = documentPermissionReadRepository.findByDocumentIdAndUserIdAndIsActiveTrue(request.getDocumentId(), request.getUserId());
            if (documentPermission.isEmpty()) {
                log.info("Document permission not found with document ID: {}, user ID: {}", request.getDocumentId(), request.getUserId());
                DocumentPermission newPermission = new DocumentPermission();
                newPermission.setDocument(documentReadRepository.findByIdAndIsActiveTrue(request.getDocumentId()).get());
                newPermission.setUser(userReadRepository.findByIdAndIsActiveTrue(request.getUserId()).get());
                newPermission.setPermission(request.getPermissionType());
                documentPermissionWriteRepository.save(newPermission);
                auditService.audit(UserActionType.DOCUMENT_PERMISSION_ADD, newPermission, String.format("Document permission added for document ID: %d and user ID: %d", request.getDocumentId(), request.getUserId()));
                return ResponseEntity.ok(ResponseDTO.success("Document permission added successfully"));
            } else {
                log.info("Document permission found with document ID: {}, user ID: {}", request.getDocumentId(), request.getUserId());
                documentPermission.get().setPermission(request.getPermissionType());
                documentPermissionWriteRepository.save(documentPermission.get());
                auditService.audit(UserActionType.DOCUMENT_PERMISSION_UPDATE, documentPermission.get(), String.format("Document permission updated for document ID: %d and user ID: %d", request.getDocumentId(), request.getUserId()));
                return ResponseEntity.ok(ResponseDTO.success("Document permission updated successfully"));
            }
        } catch (Exception e) {
            log.error("Exception occurred in document permission update process", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseDTO.error("Error occurred in document permission update process"));
        }
    }

}
