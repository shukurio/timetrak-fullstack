package com.timetrak.service;


import com.timetrak.dto.request.EmployeeRequestDTO;
import com.timetrak.dto.response.EmployeeResponseDTO;
import com.timetrak.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface EmployeeService {

    // Basic CRUD operations
    EmployeeResponseDTO getEmployeeById(Long id);
    EmployeeResponseDTO getEmployeeByUsername(String username);
    Page<EmployeeResponseDTO> getAllEmployees(Pageable pageable);
    List<EmployeeResponseDTO> getAllActiveEmployees();
    EmployeeResponseDTO updateEmployee(Long id, EmployeeResponseDTO EmployeeResponseDTO);
    void deleteEmployee(Long id);

    // Employee management
    void activateEmployee(Long id);
    void deactivateEmployee(Long id);
    List<EmployeeResponseDTO> getActiveEmployees();
    List<EmployeeResponseDTO> searchEmployees(String query);
    List<EmployeeResponseDTO> getEmployeesByDepartment(Long departmentId);

    // Legacy methods (keeping for backward compatibility)
    EmployeeResponseDTO registerEmployee(EmployeeRequestDTO dto);
    Optional<Employee> findByUsername(String username);
    //TODO login Auth Request
    //TODO Employee Statistics
}

