package com.vish.enterprise_rag.repositories.read;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vish.enterprise_rag.entities.Document;
import com.vish.enterprise_rag.enums.DocumentStatus;
import com.vish.enterprise_rag.enums.UserDesignation;

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

    @EntityGraph(attributePaths = {"owner", "organization"})
    @Query("""
        SELECT DISTINCT d FROM Document d 
        WHERE d.organization.id = :orgId 
          AND d.isActive = true 
          AND (
              d.owner.id = :userId 
              OR d.owner.designation = :employeeDesignation 
              OR d.id IN (
                  SELECT dp.document.id FROM DocumentPermission dp 
                  WHERE dp.user.id = :userId AND dp.isActive = true
              )
          )
    """)
    List<Document> findManagerVisibleDocuments(
        @Param("orgId") Long orgId, 
        @Param("userId") Long userId, 
        @Param("employeeDesignation") UserDesignation employeeDesignation
    );

    @EntityGraph(attributePaths = {"owner", "organization"})
    @Query("""
        SELECT DISTINCT d FROM Document d 
        WHERE d.organization.id = :orgId 
          AND d.isActive = true 
          AND (
              d.owner.id = :userId 
              OR d.id IN (
                  SELECT dp.document.id FROM DocumentPermission dp 
                  WHERE dp.user.id = :userId AND dp.isActive = true
              )
          )
    """)
    List<Document> findEmployeeVisibleDocuments(
        @Param("orgId") Long orgId, 
        @Param("userId") Long userId
    );
}
