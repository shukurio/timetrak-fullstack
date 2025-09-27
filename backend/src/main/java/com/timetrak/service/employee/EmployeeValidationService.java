package com.timetrak.service.employee;

import com.timetrak.dto.company.AdminRegRequestDTO;
import com.timetrak.dto.employee.EmployeeRequestDTO;
import com.timetrak.dto.employee.EmployeeUpdateDTO;
import com.timetrak.entity.Employee;
import com.timetrak.exception.employee.DuplicateEmployeeException;
import com.timetrak.exception.employee.EmployeeValidationException;
import com.timetrak.exception.employee.InvalidEmployeeException;
import com.timetrak.repository.DepartmentRepository;
import com.timetrak.repository.EmployeeRepository;
import com.timetrak.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployeeValidationService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final ShiftRepository shiftRepository;

    // ========== REGISTRATION VALIDATION ==========

    public void validateUserRegistration(EmployeeRequestDTO dto) {
        dto.normalize();
        validatePassword(dto.getUsername(),dto.getPassword());
        validateUniqueness(dto.getUsername(), dto.getEmail());
        validateDepartmentExists(dto.getDepartmentId());
    }

    public void validateAdminRegistration(AdminRegRequestDTO dto) {
        dto.normalize();
        validatePassword(dto.getUsername(),dto.getPassword());
        validateUniqueness(dto.getUsername(), dto.getEmail());
    }



    public void validatePassword(String username, String password) {
        if (password.toLowerCase().contains(username.toLowerCase())) {
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

    public void validateDeactivation(Employee employee) {
        if (employee.isDeleted()) {
            throw new InvalidEmployeeException("Cannot deactivate deleted employee", employee.getId());
        }

        if (employee.isDeactivated()) {
            throw new InvalidEmployeeException("Employee is already deactivated", employee.getId());
        }

        // Only active employees can be deactivated
        if (!employee.isActive()) {
            throw new InvalidEmployeeException("Only active employees can be deactivated", employee.getId());
        }

        validateNoActiveShifts(employee.getId(), "Cannot deactivate employee with active shifts");
    }


    public void validateRejection(Employee employee) {
        if (employee.isDeleted()) {
            throw new InvalidEmployeeException("Cannot reject deleted employee", employee.getId());
        }

        if (employee.isRejected()) {
            throw new InvalidEmployeeException("Employee is already rejected", employee.getId());
        }

        // Only pending employees can be rejected
        if (!employee.isPending()) {
            throw new InvalidEmployeeException("Only pending employees can be rejected", employee.getId());
        }

        validateNoActiveShifts(employee.getId(), "Cannot reject employee with active shifts");
    }

    public void validateApproval(Employee employee) {
        if (employee.isDeleted()) {
            throw new InvalidEmployeeException("Cannot approve deleted employee", employee.getId());
        }

        if (employee.isActive()) {
            throw new InvalidEmployeeException("Employee is already active", employee.getId());
        }

        // Only pending employees can be approved
        if (!employee.isPending()) {
            throw new InvalidEmployeeException("Only pending employees can be approved", employee.getId());
        }
    }

    public void validateReactivation(Employee employee) {
        if (employee.isDeleted()) {
            throw new InvalidEmployeeException("Cannot reactivate deleted employee", employee.getId());
        }

        if (employee.isActive() || employee.isPending()) {
            throw new InvalidEmployeeException("Employee is already active or pending approval", employee.getId());
        }

        // Can reactivate from REJECTED or DEACTIVATED
        if (!employee.isRejected() && !employee.isDeactivated()) {
            throw new InvalidEmployeeException("Employee must be rejected or deactivated to request reactivation", employee.getId());
        }

        validateNoActiveShifts(employee.getId(), "Cannot reactivate employee with active shifts");
    }


    // ========== UPDATE VALIDATION ==========

    public void validateUpdate(Employee existing, EmployeeUpdateDTO dto) {
        validateEmployeeNotDeleted(existing);
        if (existing.isRejected()) {
            throw new InvalidEmployeeException("Rejected employees can only be reactivated, not updated", existing.getId());
        }

        if (dto.getEmail() != null && !dto.getEmail().equals(existing.getEmail())) {
            if (employeeRepository.existsByEmailAndIdNot(dto.getEmail(), existing.getId())) {
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
        if (employee.isDeleted()) {
            throw new InvalidEmployeeException("Cannot activate deleted employee", employee.getId());
        }

        if (employee.isActive()) {
            throw new InvalidEmployeeException("Employee is already active", employee.getId());
        }

        //Validate which statuses can be activated
        if (!employee.isPending() && !employee.isDeactivated() && !employee.isRejected()) {
            throw new InvalidEmployeeException("Employee must be in PENDING, DEACTIVATED, or REJECTED status to activate", employee.getId());
        }
    }


    // ========== COMMON VALIDATIONS ==========

    public void validateEmployeeNotDeleted(Employee employee) {
        if (employee.isDeleted()) {
            throw new InvalidEmployeeException("Cannot update deleted employee", employee.getId());
        }
    }

    public void validateNoActiveShifts(Long employeeId, String errorMessage) {
        if (shiftRepository.hasActiveShifts(employeeId)) {
            throw new InvalidEmployeeException(errorMessage, employeeId);
        }
    }

    // ========== INPUT VALIDATION ==========

    public void validateSearchQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new EmployeeValidationException("Search query cannot be empty");
        }
    }

    public void validateDepartmentExists(Long departmentId) {
        if (departmentId == null || departmentId <= 0 || !departmentRepository.existsById(departmentId)) {
            throw new EmployeeValidationException("Invalid department ID: " + departmentId);
        }
    }
}