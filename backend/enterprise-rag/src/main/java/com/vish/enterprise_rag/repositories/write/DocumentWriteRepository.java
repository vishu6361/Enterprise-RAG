package com.vish.enterprise_rag.repositories.write;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vish.enterprise_rag.entities.Document;

public interface DocumentWriteRepository extends JpaRepository<Document, Long> {
}
