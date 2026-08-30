package com.vish.enterprise_rag.repositories.write;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vish.enterprise_rag.entities.DocumentOwnershipHistory;

public interface DocumentOwnershipHistoryWriteRepository extends JpaRepository<DocumentOwnershipHistory, Long> {
}
