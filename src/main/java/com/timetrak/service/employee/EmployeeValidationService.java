package com.timetrak.service.employee;

import com.timetrak.dto.request.EmployeeRequestDTO;
import com.timetrak.entity.Employee;
import com.timetrak.enums.Role;
import com.timetrak.exception.employee.DuplicateEmployeeException;
import com.timetrak.exception.employee.EmployeeValidationException;
import com.timetrak.exception.employee.InvalidEmployeeException;
import com.timetrak.repository.EmployeeRepository;
import com.timetrak.service.DepartmentService;
import com.timetrak.service.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployeeValidationService {

    private final EmployeeRepository employeeRepository;
    private final ShiftService shiftService;
    private final DepartmentService departmentService;

    // ========== REGISTRATION VALIDATION ==========

    public void validateRegistration(EmployeeRequestDTO dto) {
        dto.normalize();
        validateBusinessRules(dto);
        validateUniqueness(dto.getUsername(), dto.getEmail());
    }

    public void validateBusinessRules(EmployeeRequestDTO dto) {
        // Admin username rule
        if ((dto.getRole() == Role.ADMIN) && !dto.getUsername().toLowerCase().contains("admin")) {
            throw new EmployeeValidationException("Admin users must have 'admin' in their username");
        }

        // Non-admin username rule
        if (dto.getRole() != Role.ADMIN && dto.getUsername().toLowerCase().contains("admin")) {
            throw new EmployeeValidationException("Only admin users can have 'admin' in their username");
        }

        // Password security
        if (dto.getPassword().toLowerCase().contains(dto.getUsername().toLowerCase())) {
            throw new EmployeeValidationException("Password cannot contain username");
        }
    }

    public void validateUniqueness(String username, String email) {
        if (employeeRepository.existsByUsername(username)) {
            throw new DuplicateEmployeeException("username", username);
        }

        if (employeeRepository.existsByEmail(email)) {
            throw new DuplicateEmployeeException("email", email);
        }
    }

    // ========== UPDATE VALIDATION ==========

    public void validateUpdate(Employee existing, EmployeeRequestDTO dto) {
        validateEmployeeNotDeleted(existing);

        if (dto.getEmail() != null && !dto.getEmail().equals(existing.getEmail())) {
            if (!employeeRepository.existsByEmailAndId(dto.getEmail(), existing.getId())) {
                throw new DuplicateEmployeeException("email", dto.getEmail());
            }
        }
    }



    // ========== STATUS CHANGE VALIDATION ==========

    public void validateDeletion(Employee employee) {
        if (employee.isDeleted()) {
            throw new InvalidEmployeeException("Employee is already deleted", employee.getId());
        }

        validateNoActiveShifts(employee.getId(), "Cannot delete employee with active shifts");
    }

    public void validateActivation(Employee employee) {
        // More comprehensive status checking
        if (employee.isDeleted()) {
            throw new InvalidEmployeeException("Cannot activate deleted employee", employee.getId());
        }

        if (employee.isActive()) {
            throw new InvalidEmployeeException("Employee is already active", employee.getId());
        }

        // Allow activation from PENDING, DEACTIVATED, or REJECTED
    }

    public void validateDeactivation(Employee employee) {
        if (employee.isDeleted()) {
            throw new InvalidEmployeeException("Cannot deactivate deleted employee", employee.getId());
        }

        if (employee.isDeactivated()) {
            throw new InvalidEmployeeException("Employee is already deactivated", employee.getId());
        }

        validateNoActiveShifts(employee.getId(), "Cannot deactivate employee with active shifts");
    }

    // ========== COMMON VALIDATIONS ==========

    public void validateEmployeeNotDeleted(Employee employee) {
        if (employee.isDeleted()) {
            throw new InvalidEmployeeException("Cannot update deleted employee", employee.getId());
        }
    }

    public void validateNoActiveShifts(Long employeeId, String errorMessage) {
        if (shiftService.hasActiveShifts(employeeId)) {
            throw new InvalidEmployeeException(errorMessage, employeeId);
        }
    }

    // ========== INPUT VALIDATION ==========

    public void validateSearchQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new EmployeeValidationException("Search query cannot be empty");
        }
    }

    //fixed department validation
    public void validateDepartmentExists(Long departmentId) {
        if (departmentId == null || departmentId <= 0 || !departmentService.existsById(departmentId)) {
            throw new EmployeeValidationException("Invalid department ID: " + departmentId);
        }
    }
}