package com.timetrak.service.employee;


import com.timetrak.dto.company.AdminRegRequestDTO;
import com.timetrak.dto.employee.EmployeeRequestDTO;
import com.timetrak.dto.employee.EmployeeResponseDTO;
import com.timetrak.dto.employee.EmployeeUpdateDTO;
import com.timetrak.entity.Company;
import com.timetrak.entity.Employee;
import com.timetrak.enums.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EmployeeService {

    // Basic CRUD operations
    Employee getById(Long id, Long companyId);
    Employee getActiveById(Long id, Long companyId);
    Page<EmployeeResponseDTO> getAllActiveForCompany(Long companyId, Pageable pageable);

    Page<EmployeeResponseDTO> getAllEmployeesForCompany(Long companyId, Pageable pageable);
    List<Long> getAllActiveEmployeeIdsForCompany(Long companyId);

    EmployeeResponseDTO updateEmployee(Long id, EmployeeUpdateDTO dto, Long companyId);

    void deleteEmployee(Long id, Long companyId);

    // Employee management
    void activateEmployee(Long id, Long companyId);
    void deactivateEmployee(Long id, Long companyId);
    Page<EmployeeResponseDTO> searchEmployees(String query,Long companyId, Pageable pageable);
    Page<EmployeeResponseDTO> getEmployeesByDepartment(Long departmentId, Long companyId, Pageable pageable);
    void approveEmployee(Long id, Long companyId);
    void rejectEmployee(Long id, Long companyId);
    void requestReactivation(Long id, Long companyId);
    Page<EmployeeResponseDTO> getByStatus(Long companyId,EmployeeStatus status, Pageable pageable);

    // Legacy methods (keeping for backward compatibility)
    EmployeeResponseDTO createEmployee(EmployeeRequestDTO dto);

    EmployeeResponseDTO createAdmin(AdminRegRequestDTO adminRequest, Company company);

    // Legacy methods (keeping for backward compatibility - NO company scope)
    EmployeeResponseDTO getEmployeeDTOById(Long id);
    EmployeeResponseDTO getByUsername(String username);
    String getEmployeeNameById(Long employeeId);
    void changePassword(String username, String oldPassword, String newPassword);

    //TODO login Auth Request
    //TODO Employee Statistics
}

