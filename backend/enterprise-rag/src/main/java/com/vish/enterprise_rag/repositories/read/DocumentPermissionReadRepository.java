package com.vish.enterprise_rag.repositories.read;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vish.enterprise_rag.entities.DocumentPermission;
import com.vish.enterprise_rag.enums.DocumentPermissionType;

public interface DocumentPermissionReadRepository extends JpaRepository<DocumentPermission, Long> {

    Optional<DocumentPermission> findByDocumentIdAndUserIdAndIsActiveTrue(Long documentId, Long userId);

    Optional<DocumentPermission> findByDocumentIdAndPermissionAndIsActiveTrue(Long documentId, DocumentPermissionType permission);

    List<DocumentPermission> findByDocumentIdAndIsActiveTrue(Long documentId);

    List<DocumentPermission> findByUserIdAndIsActiveTrue(Long userId);

    boolean existsByDocumentIdAndUserIdAndPermissionAndIsActiveTrue(Long documentId, Long userId, DocumentPermissionType permission);

    boolean existsByDocumentIdAndUserIdAndIsActiveTrue(Long documentId, Long userId);
}
