package com.vish.enterprise_rag.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.vish.enterprise_rag.entities.Document;
import com.vish.enterprise_rag.enums.DocumentStatus;
import com.vish.enterprise_rag.repositories.read.DocumentReadRepository;
import com.vish.enterprise_rag.repositories.read.OrganizationReadRepository;
import com.vish.enterprise_rag.repositories.read.UserReadRepository;
import com.vish.enterprise_rag.repositories.write.DocumentWriteRepository;
import com.vish.enterprise_rag.response.ResponseDTO;
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

    @Override
    public ResponseEntity<?> uploadDocument(MultipartFile file) {
        log.info("Document upload initiated for file: {}, size: {} bytes", file.getOriginalFilename(), file.getSize());
        try {
            String contentHash = CommonUtils.calculateSHA256(file.getBytes());

            if (documentReadRepository.findByOrganizationIdAndContentHashAndIsActiveTrue(3L, contentHash).isPresent()) {
                log.info("Document already exists with SHA-256 content hash for file: {}", file.getOriginalFilename());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(ResponseDTO.error("Document already exists"));
            }
            Document document = new Document();
            document.setOrganization(organizationReadRepository.findByIdAndIsActiveTrue(3L).get());
            document.setContentHash(contentHash);
            document.setExtension(file.getContentType());
            document.setIsActive(true);
            document.setDocumentName(file.getOriginalFilename());
            document.setOwner(userReadRepository.findByIdAndIsActiveTrue(1L).get());
            document.setStatus(DocumentStatus.PROCESSING);
            documentWriteRepository.save(document);
            return ResponseEntity.ok(ResponseDTO.success("Document uploaded successfully"));
        } catch (Exception e) {
            log.error("Exception occurred in file upload process", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseDTO.error("Error occurred in file upload process"));
        }
    }
}
