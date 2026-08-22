package com.vish.enterprise_rag.service;

import org.springframework.http.ResponseEntity;

import com.vish.enterprise_rag.requests.OrganizationReq;

public interface OrganizationService {

    ResponseEntity<?> createOrganization(OrganizationReq request);

    ResponseEntity<?> updateOrganization(long id, OrganizationReq request);

    ResponseEntity<?> deleteOrganization(long id);

    ResponseEntity<?> getAllOrganizations();

    ResponseEntity<?> getOrganization(long id);

}
