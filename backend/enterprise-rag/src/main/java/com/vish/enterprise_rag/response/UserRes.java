package com.vish.enterprise_rag.response;

import com.vish.enterprise_rag.enums.UserDesignation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserRes {
    private Long id;
    private String name;
    private String email;
    private Long organizationId;
    private UserDesignation designation;
}
