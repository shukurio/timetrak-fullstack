
package com.timetrak.controller;


import com.timetrak.dto.response.EmployeeResponseDTO;
import com.timetrak.dto.request.EmployeeRequestDTO;
import com.timetrak.enums.EmployeeStatus;
import com.timetrak.service.auth.AuthContextService;
import com.timetrak.service.employee.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employees")
@PreAuthorize("hasRole('ADMIN')")
public class AdminEmployeeController {

    private final EmployeeService employeeService;
    private final AuthContextService authContextService;

    private Long getCompanyId() {
        return authContextService.getCurrentCompanyId();
    }

    @Operation(summary = "Get all employees", description = "Get all employees with pagination")
    @GetMapping("/all")
    public ResponseEntity<Page<EmployeeResponseDTO>> getAllEmployees(Pageable pageable) {
        Page<EmployeeResponseDTO> employees = employeeService
                .getAllEmployeesForCompany(getCompanyId(),pageable);
        return ResponseEntity.ok(employees);
    }

    @Operation(summary = "Get all Active employees", description = "Get all employees with pagination")
    @GetMapping("/active")
    public ResponseEntity<Page<EmployeeResponseDTO>> getAllActiveEmployees(Pageable pageable) {

        Page<EmployeeResponseDTO> employees = employeeService
                .getAllActiveForCompany(getCompanyId(), pageable);
        return ResponseEntity.ok(employees);
    }


    @Operation(summary = "Get employee by ID", description = "Get a specific employee by ID")
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(@PathVariable Long id) {
        EmployeeResponseDTO employee = employeeService.getEmployeeDTOById(id);
        return ResponseEntity.ok(employee);
    }


    @Operation(summary = "Deactivate employee", description = "Deactivate an employee account")
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Map<String, String>> deactivateEmployee(@PathVariable Long id) {
        employeeService.deactivateEmployee(id);
        return ResponseEntity.ok(Map.of("message", "Employee deactivated successfully"));
    }

    @Operation(summary = "Activate employee", description = "Activate an employee account")
    @PutMapping("/{id}/activate")
    public ResponseEntity<Map<String, String>> activateEmployee(@PathVariable Long id) {
        employeeService.activateEmployee(id);
        return ResponseEntity.ok(Map.of("message", "Employee activated successfully"));
    }

    @Operation(summary = "Search employees", description = "Search employees by name, username, or email")
    @GetMapping("/search")
    public ResponseEntity<Page<EmployeeResponseDTO>> searchEmployees(@RequestParam String query, Pageable pageable) {
        Page<EmployeeResponseDTO> employees = employeeService
                .searchEmployees(query,getCompanyId(), pageable);
        return ResponseEntity.ok(employees);
    }


    @Operation(summary = "Get employees by department", description = "Get all employees in a specific department")
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<Page<EmployeeResponseDTO>> getEmployeesByDepartment
            (@PathVariable Long departmentId, Pageable pageable) {
        Page<EmployeeResponseDTO> employees = employeeService.getEmployeesByDepartment(departmentId, pageable);
        return ResponseEntity.ok(employees);
    }

    @PostMapping("/register")
    public ResponseEntity<EmployeeResponseDTO> registerEmployee(@Valid @RequestBody EmployeeRequestDTO request) {
        EmployeeResponseDTO employee = employeeService.registerEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(employee);  // 201 CREATED
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();  // 204
    }

    @Operation(summary = "Approve pending employee", description = "Approve a pending employee application")
    @PutMapping("/{id}/approve")
    public ResponseEntity<Map<String, String>> approveEmployee(@PathVariable Long id) {
        employeeService.approveEmployee(id);
        return ResponseEntity.ok(Map.of("message", "Employee approved successfully"));
    }

    @Operation(summary = "Reject pending employee", description = "Reject a pending employee application")
    @PutMapping("/{id}/reject")
    public ResponseEntity<Map<String, String>> rejectEmployee(@PathVariable Long id) {
        employeeService.rejectEmployee(id);
        return ResponseEntity.ok(Map.of("message", "Employee rejected successfully"));
    }

    @Operation(summary = "Request reactivation", description = "Request reactivation for rejected/deactivated employee")
    @PutMapping("/{id}/reactivate")
    public ResponseEntity<Map<String, String>> requestReactivation(@PathVariable Long id) {
        employeeService.requestReactivation(id);
        return ResponseEntity.ok(Map.of("message", "Reactivation requested successfully"));
    }

    @Operation(
            summary = "Get employees by status",
            description = "Get all employees filtered by their current status"
    )
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<EmployeeResponseDTO>> getEmployeesByStatus(
            @Parameter(description = "Employee status to filter by", example = "PENDING")
            @PathVariable EmployeeStatus status,
            Pageable pageable) {

        Page<EmployeeResponseDTO> employees = employeeService
                .getByStatus(getCompanyId(), status, pageable);
        return ResponseEntity.ok(employees);
    }

    @Operation(summary = "Update employee", description = "Update employee information")
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequestDTO request) {
        EmployeeResponseDTO employee = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(employee);
    }
}

