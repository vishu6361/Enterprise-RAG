package com.vish.enterprise_rag.requests;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
    @NotBlank(message = "Organization name is required")
    private String name;

    private String address;

    @Email(message = "Invalid contact email format")
    private String contactEmail;

    private String contactPhone;

    @AssertTrue(message = "Either contactEmail or contactPhone must be provided")
    public boolean isContactProvided() {
        return (contactEmail != null && !contactEmail.trim().isEmpty())
                || (contactPhone != null && !contactPhone.trim().isEmpty());
    }
}
