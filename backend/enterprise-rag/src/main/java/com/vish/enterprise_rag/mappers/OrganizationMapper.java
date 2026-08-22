package com.vish.enterprise_rag.mappers;

import org.springframework.stereotype.Component;

import com.vish.enterprise_rag.entities.Organization;
import com.vish.enterprise_rag.requests.OrganizationReq;
import com.vish.enterprise_rag.response.OrganizationRes;

@Component
public class OrganizationMapper {
    public Organization toEntity(OrganizationReq request) {
        Organization organization = new Organization();
        organization.setName(request.getName());
        organization.setAddress(request.getAddress());
        organization.setContactEmail(request.getContactEmail());
        organization.setContactPhone(request.getContactPhone());
        return organization;
    }

    public OrganizationReq toReq(Organization organization){
        return new OrganizationReq(organization.getName(), organization.getAddress(), organization.getContactEmail(), organization.getContactPhone());
    }

    public OrganizationRes toRes(Organization organization){
        return new OrganizationRes(organization.getId(), organization.getName(), organization.getAddress(), organization.getContactEmail(), organization.getContactPhone());
    }  
}
