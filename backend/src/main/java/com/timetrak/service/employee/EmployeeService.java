package com.timetrak.service.employee;


import com.timetrak.dto.company.AdminRegRequestDTO;
import com.timetrak.dto.request.EmployeeRequestDTO;
import com.timetrak.dto.response.EmployeeResponseDTO;
import com.timetrak.entity.Company;
import com.timetrak.entity.Employee;
import com.timetrak.enums.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EmployeeService {

    // Basic CRUD operations
    EmployeeResponseDTO getEmployeeDTOById(Long id, Long companyId);
    Employee getById(Long id, Long companyId);
    Employee getActiveById(Long id, Long companyId);
    List<Employee> getByIds(List<Long> ids, Long companyId);
    Page<EmployeeResponseDTO> getAllActiveForCompany(Long companyId, Pageable pageable);

    EmployeeResponseDTO getByUsername(String username, Long companyId);
    Employee getByEmail(String email, Long companyId);
    Page<EmployeeResponseDTO> getAllEmployeesForCompany(Long companyId, Pageable pageable);
    List<Long> getAllActiveEmployeeIdsForCompany(Long companyId);

    EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO dto, Long companyId);
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

    String getEmployeeNameById(Long employeeId, Long companyId);
    
    // Legacy methods (keeping for backward compatibility - NO company scope)
    EmployeeResponseDTO getEmployeeDTOById(Long id);
    EmployeeResponseDTO getByUsername(String username);
    Employee getByEmail(String email);
    String getEmployeeNameById(Long employeeId);
    //TODO login Auth Request
    //TODO Employee Statistics
}

