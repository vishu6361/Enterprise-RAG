package com.vish.enterprise_rag.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.vish.enterprise_rag.requests.DocumentPermissionUpdateReq;

public interface DocumentService {

    ResponseEntity<?> uploadDocument(MultipartFile file);

    ResponseEntity<?> deleteDocument(Long id);

    ResponseEntity<?> updatePermission(DocumentPermissionUpdateReq request);
}
