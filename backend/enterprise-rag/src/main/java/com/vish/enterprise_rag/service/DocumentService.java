package com.vish.enterprise_rag.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentService {

    ResponseEntity<?> uploadDocument(MultipartFile file);
}
