package com.vish.enterprise_rag.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * UserReq
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserReq {

    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotNull(message = "Organization ID is required")
    private Long organizationId;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Designation is required")
    private String designation;
}
