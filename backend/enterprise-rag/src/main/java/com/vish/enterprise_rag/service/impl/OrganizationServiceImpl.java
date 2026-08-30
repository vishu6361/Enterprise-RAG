package com.vish.enterprise_rag.service.impl;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vish.enterprise_rag.entities.Organization;
import com.vish.enterprise_rag.enums.UserActionType;
import com.vish.enterprise_rag.mappers.OrganizationMapper;
import com.vish.enterprise_rag.repositories.read.OrganizationReadRepository;
import com.vish.enterprise_rag.repositories.write.OrganizationWriteRepository;
import com.vish.enterprise_rag.requests.OrganizationReq;
import com.vish.enterprise_rag.response.ResponseDTO;
import com.vish.enterprise_rag.security.SecurityUtils;
import com.vish.enterprise_rag.service.AuditService;
import com.vish.enterprise_rag.service.OrganizationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationWriteRepository organizationWriteRepository;
    private final OrganizationMapper organizationMapper;
    private final OrganizationReadRepository organizationReadRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public ResponseEntity<?> createOrganization(OrganizationReq request) {
        log.info("Creating organization: {}", request);
        try {
            boolean hasEmail = request.getContactEmail() != null && !request.getContactEmail().trim().isEmpty();
            boolean hasPhone = request.getContactPhone() != null && !request.getContactPhone().trim().isEmpty();

            if (!hasEmail && !hasPhone) {
                return ResponseEntity.ok(ResponseDTO.error("Either contactEmail or contactPhone must be provided"));
            }

            if ((hasEmail && organizationReadRepository.findByContactEmailAndIsActiveTrue(request.getContactEmail()).isPresent()) ||
                    (hasPhone && organizationReadRepository.findByContactPhoneAndIsActiveTrue(request.getContactPhone()).isPresent())) {
                return ResponseEntity.ok(ResponseDTO.error("Organization already exists with given contact info"));
            }
            Organization organization = organizationMapper.toEntity(request);
            organization = organizationWriteRepository.save(organization);
            auditService.audit(UserActionType.ORGANIZATION_CREATE, organization, "Created new organization: " + organization.getName());
            return ResponseEntity.ok(ResponseDTO.success("Organization created successfully", organizationMapper.toRes(organization)));
        } catch (Exception e) {
            log.error("Error occurred while creating organization", e);
            return ResponseEntity.ok(ResponseDTO.error(e.getMessage()));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> updateOrganization(long id, OrganizationReq request) {
        log.info("Updating organization: {}", request);
        try {
            Long currentOrgId = SecurityUtils.getCurrentOrganizationId();
            if (currentOrgId != null && !currentOrgId.equals(id) && !SecurityUtils.isAdmin()) {
                return ResponseEntity.ok(ResponseDTO.error("You do not have permission to update another organization"));
            }

            Optional<Organization> existingOrg = organizationReadRepository.findByIdAndIsActiveTrue(id);
            if (existingOrg.isEmpty()) {
                return ResponseEntity.ok(ResponseDTO.error("Organization not found with ID: " + id));
            }
            Organization org = existingOrg.get();
            if (request.getName() != null && !request.getName().trim().isEmpty()) {
                org.setName(request.getName());
            }
            if (request.getAddress() != null && !request.getAddress().trim().isEmpty()) {
                org.setAddress(request.getAddress());
            }
            if (request.getContactEmail() != null && !request.getContactEmail().trim().isEmpty()) {
                org.setContactEmail(request.getContactEmail());
            }
            if (request.getContactPhone() != null && !request.getContactPhone().trim().isEmpty()) {
                org.setContactPhone(request.getContactPhone());
            }
            organizationWriteRepository.save(org);
            auditService.audit(UserActionType.ORGANIZATION_EDIT, org, "Updated organization: " + org.getName());
            return ResponseEntity.ok(ResponseDTO.success("Organization updated successfully", organizationMapper.toRes(org)));
        } catch (Exception e) {
            log.error("Exception occurred in updating organization with ID {}", id, e);
            return ResponseEntity.ok(ResponseDTO.error("Error occurred while processing organization update"));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> deleteOrganization(long id) {
        try {
            Long currentOrgId = SecurityUtils.getCurrentOrganizationId();
            if (currentOrgId != null && !currentOrgId.equals(id) && !SecurityUtils.isAdmin()) {
                return ResponseEntity.ok(ResponseDTO.error("You do not have permission to delete another organization"));
            }

            Optional<Organization> existingOrg = organizationReadRepository.findByIdAndIsActiveTrue(id);
            if (existingOrg.isEmpty()) {
                return ResponseEntity.ok(ResponseDTO.error("Organization not found with ID: " + id));
            }
            Organization org = existingOrg.get();
            org.setIsActive(false);
            organizationWriteRepository.save(org);
            auditService.audit(UserActionType.ORGANIZATION_DELETE, org, "Deleted organization: " + org.getName());
            return ResponseEntity.ok(ResponseDTO.success("Organization deleted successfully", null));
        } catch (Exception e) {
            log.error("Exception occurred in deleting organization with ID {}", id, e);
            return ResponseEntity.ok(ResponseDTO.error("Error occurred while processing organization deletion"));
        }
    }

    @Override
    public ResponseEntity<?> getAllOrganizations() {
        try {
            return ResponseEntity.ok(ResponseDTO.success("Organizations fetched successfully", organizationReadRepository.findByIsActiveTrue().stream().map(organizationMapper::toRes).toList()));
        } catch (Exception e) {
            log.error("Exception occurred in getting all organizations", e);
            return ResponseEntity.ok(ResponseDTO.error("Error occurred while processing organization retrieval"));
        }
    }

    @Override
    public ResponseEntity<?> getOrganization(long id) {
        try {
            Long currentOrgId = SecurityUtils.getCurrentOrganizationId();
            if (currentOrgId != null && !currentOrgId.equals(id) && !SecurityUtils.isAdmin()) {
                return ResponseEntity.ok(ResponseDTO.error("Organization not found"));
            }
            Optional<Organization> existingOrg = organizationReadRepository.findByIdAndIsActiveTrue(id);
            if (existingOrg.isEmpty()) {
                return ResponseEntity.ok(ResponseDTO.error("Organization not found with ID: " + id));
            }
            return ResponseEntity.ok(ResponseDTO.success("Organization found successfully", organizationMapper.toRes(existingOrg.get())));
        } catch (Exception e) {
            log.error("Exception occurred in getting organization with ID {}", id, e);
            return ResponseEntity.ok(ResponseDTO.error("Error occurred while processing organization retrieval"));
        }
    }
}
