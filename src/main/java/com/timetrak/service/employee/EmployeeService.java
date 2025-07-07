package com.timetrak.service.employee;


import com.timetrak.dto.request.EmployeeRequestDTO;
import com.timetrak.dto.response.EmployeeResponseDTO;
import com.timetrak.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {

    // Basic CRUD operations
    EmployeeResponseDTO getEmployeeDTOById(Long id);
    Employee getById(Long id);

    EmployeeResponseDTO getEmployeeDTOByUsername(String username);
    Employee getByUsername(String username);
    Employee getByEmail(String email);
    Page<EmployeeResponseDTO> getAllEmployees(Pageable pageable);
    Page<EmployeeResponseDTO> getAllActiveEmployees(Pageable pageable);
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

    // Legacy methods (keeping for backward compatibility)
    EmployeeResponseDTO registerEmployee(EmployeeRequestDTO dto);
    //TODO login Auth Request
    //TODO Employee Statistics

    String getEmployeeNameById(Long id);
}

