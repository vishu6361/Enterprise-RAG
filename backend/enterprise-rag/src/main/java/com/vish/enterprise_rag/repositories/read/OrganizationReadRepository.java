package com.vish.enterprise_rag.repositories.read;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vish.enterprise_rag.entities.Organization;

public interface OrganizationReadRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findByContactEmailAndIsActiveTrue(String contactEmail);
    Optional<Organization> findByContactPhoneAndIsActiveTrue(String contactPhone);
    Optional<Organization> findByIdAndIsActiveTrue(Long id);
    List<Organization> findByIsActiveTrue();
}
