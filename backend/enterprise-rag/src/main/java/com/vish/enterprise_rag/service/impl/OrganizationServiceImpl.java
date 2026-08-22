package com.vish.enterprise_rag.service.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.vish.enterprise_rag.entities.Organization;
import com.vish.enterprise_rag.mappers.OrganizationMapper;
import com.vish.enterprise_rag.repositories.write.OrganizationWriteRepository;
import com.vish.enterprise_rag.requests.OrganizationReq;
import com.vish.enterprise_rag.response.ResponseDTO;
import com.vish.enterprise_rag.service.OrganizationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationWriteRepository organizationWriteRepository;
    private final OrganizationMapper organizationMapper;

    @Override
    public ResponseEntity<?> createOrganization(OrganizationReq request) {
        log.info("Creating organization: {}", request);
        Organization organization = organizationMapper.toEntity(request);
        organizationWriteRepository.save(organization);
        return ResponseEntity.ok(ResponseDTO.success("Organization created successfully", organization));
    }

    @Override
    public ResponseEntity<?> updateOrganization(long id, OrganizationReq request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateOrganization'");
    }

    @Override
    public ResponseEntity<?> deleteOrganization(long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteOrganization'");
    }

    @Override
    public ResponseEntity<?> getAllOrganizations() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllOrganizations'");
    }

    @Override
    public ResponseEntity<?> getOrganization(long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getOrganization'");
    }

}
