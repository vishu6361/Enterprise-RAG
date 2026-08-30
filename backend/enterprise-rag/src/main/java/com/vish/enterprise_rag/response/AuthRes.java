package com.vish.enterprise_rag.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AuthRes {
    private String token;
    @Builder.Default
    private String tokenType = "Bearer";
    private UserRes user;
}
