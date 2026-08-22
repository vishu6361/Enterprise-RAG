package com.vish.enterprise_rag.mappers;

import org.springframework.stereotype.Component;

import com.vish.enterprise_rag.entities.User;
import com.vish.enterprise_rag.repositories.read.OrganizationReadRepository;
import com.vish.enterprise_rag.requests.UserReq;
import com.vish.enterprise_rag.response.UserRes;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserMapper {
    private final OrganizationReadRepository organizationReadRepository;

    public User toEntity(UserReq userReq) {
        User user = new User();
        user.setName(userReq.getName());
        user.setEmail(userReq.getEmail());
        user.setPassword(userReq.getPassword());
        user.setDesignation(userReq.getDesignation());
        user.setOrganization(organizationReadRepository.findById(userReq.getOrganizationId()).orElseThrow(()->new RuntimeException("Organization not found")));
        return user;
    }   
    
    public UserRes toRes(User user){
        UserRes userRes = new UserRes();
        userRes.setId(user.getId());
        userRes.setName(user.getName());
        userRes.setEmail(user.getEmail());
        userRes.setOrganizationId(user.getOrganization().getId());
        userRes.setDesignation(user.getDesignation());
        return userRes;
    }
}
