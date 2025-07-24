package com.timetrak.service.employee;


import com.timetrak.dto.request.EmployeeRequestDTO;
import com.timetrak.dto.response.EmployeeResponseDTO;
import com.timetrak.entity.Employee;
import com.timetrak.enums.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EmployeeService {

    // Basic CRUD operations
    EmployeeResponseDTO getEmployeeDTOById(Long id);
    Employee getById(Long id);
    List<Employee> getByIds(List<Long> ids);
    Page<EmployeeResponseDTO> getAllActiveInCurrentCompany(Pageable pageable);

    Employee getByUsername(String username);
    Employee getByEmail(String email);
    Page<EmployeeResponseDTO> getAllEmployeesForCompany(Pageable pageable);
    List<Long> getAllActiveEmployeeIdsForCompany();

    EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO dto);
    void deleteEmployee(Long id);

    // Employee management
    void activateEmployee(Long id);
    void deactivateEmployee(Long id);
    Page<EmployeeResponseDTO> searchEmployees(String query, Pageable pageable);
    Page<EmployeeResponseDTO> getEmployeesByDepartment(Long departmentId, Pageable pageable);
    void approveEmployee(Long id);
    void rejectEmployee(Long id);
    void requestReactivation(Long id);
    Page<EmployeeResponseDTO> getByStatus(EmployeeStatus status, Pageable pageable);

    // Legacy methods (keeping for backward compatibility)
    EmployeeResponseDTO registerEmployee(EmployeeRequestDTO dto);

    String getEmployeeNameById(Long employeeId);
    //TODO login Auth Request
    //TODO Employee Statistics
}

