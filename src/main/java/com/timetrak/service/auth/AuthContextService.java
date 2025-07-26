package com.timetrak.service.auth;

import com.timetrak.enums.Role;
import com.timetrak.exception.UnauthorizedAccessException;
import com.timetrak.security.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthContextService {


    public Long getCurrentCompanyId() {
        return getCurrentUserDetails().getCompanyId();
    }

    public Long getCurrentEmployeeId() {
        return getCurrentUserDetails().getEmployee().getId();
    }

    public String getCurrentUsername() {
        return getAuthentication().getName();
    }

    public Role getCurrentUserRole() {
        return getCurrentUserDetails().getEmployee().getRole();
    }


    public CustomUserDetails getCurrentUserDetails() {
        Authentication authentication = getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedAccessException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails)) {
            throw new UnauthorizedAccessException("Invalid authentication principal type");
        }

        return (CustomUserDetails) principal;
    }

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}