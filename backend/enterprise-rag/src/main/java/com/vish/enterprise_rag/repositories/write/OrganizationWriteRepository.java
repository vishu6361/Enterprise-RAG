package com.vish.enterprise_rag.repositories.write;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vish.enterprise_rag.entities.Organization;

@Repository
public interface OrganizationWriteRepository extends JpaRepository<Organization, Long> {
    
}
