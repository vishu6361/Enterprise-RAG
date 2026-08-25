package com.vish.enterprise_rag.repositories.read;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vish.enterprise_rag.entities.Document;
import com.vish.enterprise_rag.enums.DocumentStatus;

@Repository
public interface DocumentReadRepository extends JpaRepository<Document, Long> {

    @EntityGraph(attributePaths = {"owner", "organization"})
    Optional<Document> findByIdAndIsActiveTrue(Long id);

    Optional<Document> findByOrganizationIdAndContentHashAndIsActiveTrue(Long organizationId, String contentHash);

    boolean existsByOrganizationIdAndContentHashAndIsActiveTrue(Long organizationId, String contentHash);

    @EntityGraph(attributePaths = {"owner", "organization"})
    List<Document> findByOrganizationIdAndIsActiveTrue(Long organizationId);

    @EntityGraph(attributePaths = {"owner", "organization"})
    List<Document> findByOwnerIdAndIsActiveTrue(Long ownerId);

    @EntityGraph(attributePaths = {"owner", "organization"})
    List<Document> findByOrganizationIdAndStatusAndIsActiveTrue(Long organizationId, DocumentStatus status);
}
