package com.vish.enterprise_rag.repositories.read;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vish.enterprise_rag.entities.DocumentOwnershipHistory;

public interface DocumentOwnershipHistoryReadRepository extends JpaRepository<DocumentOwnershipHistory, Long> {

    List<DocumentOwnershipHistory> findByDocumentIdOrderByOwnershipChangedAtDesc(Long documentId);

    List<DocumentOwnershipHistory> findByNewUserIdOrderByOwnershipChangedAtDesc(Long newUserId);

    List<DocumentOwnershipHistory> findByOldUserIdOrderByOwnershipChangedAtDesc(Long oldUserId);
}
