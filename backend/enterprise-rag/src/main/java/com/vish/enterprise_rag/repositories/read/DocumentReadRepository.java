package com.vish.enterprise_rag.repositories.read;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vish.enterprise_rag.entities.Document;
import com.vish.enterprise_rag.enums.DocumentStatus;

public interface DocumentReadRepository extends JpaRepository<Document, Long> {

    Optional<Document> findByIdAndIsActiveTrue(Long id);

    Optional<Document> findByOrganizationIdAndContentHashAndIsActiveTrue(Long organizationId, String contentHash);

    boolean existsByOrganizationIdAndContentHashAndIsActiveTrue(Long organizationId, String contentHash);

    List<Document> findByOrganizationIdAndIsActiveTrue(Long organizationId);

    List<Document> findByOwnerIdAndIsActiveTrue(Long ownerId);

    List<Document> findByOrganizationIdAndStatusAndIsActiveTrue(Long organizationId, DocumentStatus status);
}
