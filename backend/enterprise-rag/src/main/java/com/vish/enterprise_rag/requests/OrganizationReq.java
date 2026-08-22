package com.vish.enterprise_rag.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * OrganizationReq
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationReq {
    private String name;
    private String address;
    private String contactEmail;
    private String contactPhone;
}
