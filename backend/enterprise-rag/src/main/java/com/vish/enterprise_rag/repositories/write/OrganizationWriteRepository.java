package com.vish.enterprise_rag.repositories.write;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vish.enterprise_rag.entities.Organization;
public interface OrganizationWriteRepository extends JpaRepository<Organization, Long> {

}
