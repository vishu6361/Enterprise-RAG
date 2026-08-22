package com.vish.enterprise_rag.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationRes {
    private Long id;
    private String name;
    private String address;
    private String contactEmail;
    private String contactPhone;
}
