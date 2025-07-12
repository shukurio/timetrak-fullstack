package com.timetrak.service.auth;

import com.timetrak.enums.Role;
import com.timetrak.exception.UnauthorizedAccessException;
import com.timetrak.repository.EmployeeRepository;
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

    private final EmployeeRepository employeeRepository;

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

    public void validateEmployeeInCompany(Long employeeId) {
        if (employeeId == null) {
            throw new UnauthorizedAccessException("Employee ID cannot be null");
        }

        Long currentCompanyId = getCurrentCompanyId();
        boolean employeeBelongsToCompany = employeeRepository
                .existsByIdAndCompanyId(employeeId, currentCompanyId);

        if (!employeeBelongsToCompany) {
            log.warn("Admin {} attempted to access employee {} from different company",
                    getCurrentEmployeeId(), employeeId);
            throw new UnauthorizedAccessException("Access denied: Employee belongs to different company");
        }
    }

    public void validateCompanyAccess(Long companyId) {
        if (companyId == null) {
            throw new UnauthorizedAccessException("Company ID cannot be null");
        }

        Long currentCompanyId = getCurrentCompanyId();
        if (!currentCompanyId.equals(companyId)) {
            log.warn("User {} attempted to access company {}, but belongs to company {}",
                    getCurrentUsername(), companyId, currentCompanyId);
            throw new UnauthorizedAccessException("Access denied: User does not belong to company " + companyId);
        }
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