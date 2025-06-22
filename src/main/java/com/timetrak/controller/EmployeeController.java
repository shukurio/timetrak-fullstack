
package com.timetrak.controller;


import com.timetrak.dto.response.EmployeeResponseDTO;
import com.timetrak.dto.request.EmployeeRequestDTO;
import com.timetrak.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;


    @Operation(summary = "Get all employees", description = "Get all employees with pagination")
    @GetMapping("/all")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<EmployeeResponseDTO>> getAllEmployees(Pageable pageable) {
        Page<EmployeeResponseDTO> employees = employeeService.getAllEmployees(pageable);
        return ResponseEntity.ok(employees);
    }

    @Operation(summary = "Get all Active employees", description = "Get all employees with pagination")
    @GetMapping("/active")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<EmployeeResponseDTO>> getAllActiveEmployees(Pageable pageable) {
        Page<EmployeeResponseDTO> employees = employeeService.getAllActiveEmployees(pageable);
        return ResponseEntity.ok(employees);
    }


    @Operation(summary = "Get employee by ID", description = "Get a specific employee by ID")
    @GetMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(@PathVariable Long id) {
        EmployeeResponseDTO employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }


    @Operation(summary = "Deactivate employee", description = "Deactivate an employee account")
    @PutMapping("/{id}/deactivate")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deactivateEmployee(@PathVariable Long id) {
        employeeService.deactivateEmployee(id);
        return ResponseEntity.ok(Map.of("message", "Employee deactivated successfully"));
    }

    @Operation(summary = "Activate employee", description = "Activate an employee account")
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> activateEmployee(@PathVariable Long id) {
        employeeService.activateEmployee(id);
        return ResponseEntity.ok(Map.of("message", "Employee activated successfully"));
    }

    @Operation(summary = "Search employees", description = "Search employees by name, username, or email")
    @GetMapping("/search")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<EmployeeResponseDTO>> searchEmployees(@RequestParam String query, Pageable pageable) {
        Page<EmployeeResponseDTO> employees = employeeService.searchEmployees(query, pageable);
        return ResponseEntity.ok(employees);
    }


    @Operation(summary = "Get employees by department", description = "Get all employees in a specific department")
    @GetMapping("/department/{departmentId}")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<EmployeeResponseDTO>> getEmployeesByDepartment
            (@PathVariable Long departmentId, Pageable pageable) {
        Page<EmployeeResponseDTO> employees = employeeService.getEmployeesByDepartment(departmentId, pageable);
        return ResponseEntity.ok(employees);
    }

    @PostMapping("/register")
    public ResponseEntity<EmployeeResponseDTO> registerEmployee(@RequestBody EmployeeRequestDTO request) {
        EmployeeResponseDTO employee = employeeService.registerEmployee(request);
        return ResponseEntity.ok(employee);
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();  // 204
    }
}

