package com.vish.enterprise_rag.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.vish.enterprise_rag.service.DocumentService;

@Slf4j
@RestController
@RequestMapping(DocumentController.BASE_URL)
@RequiredArgsConstructor
public class DocumentController {
    public static final String BASE_URL = "/api/v1/documents";

    private final DocumentService documentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadDocument(MultipartFile file) {
        log.info("Document upload initiated for file: {}, size: {} bytes", file.getOriginalFilename(), file.getSize());
        return documentService.uploadDocument(file);
    }
}
