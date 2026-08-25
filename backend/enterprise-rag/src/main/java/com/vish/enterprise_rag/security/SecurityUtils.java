package com.vish.enterprise_rag.security;

import com.vish.enterprise_rag.enums.UserDesignation;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<UserPrincipal> getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return Optional.of(principal);
        }
        return Optional.empty();
    }

    public static Long getCurrentUserId() {
        return getCurrentUserPrincipal().map(UserPrincipal::getId).orElse(null);
    }

    public static Long getCurrentOrganizationId() {
        return getCurrentUserPrincipal().map(UserPrincipal::getOrganizationId).orElse(null);
    }

    public static String getCurrentUserEmail() {
        return getCurrentUserPrincipal().map(UserPrincipal::getEmail).orElse(null);
    }

    public static UserDesignation getCurrentUserDesignation() {
        return getCurrentUserPrincipal().map(UserPrincipal::getDesignation).orElse(null);
    }

    public static boolean isAdmin() {
        return UserDesignation.ADMIN.equals(getCurrentUserDesignation());
    }
}
