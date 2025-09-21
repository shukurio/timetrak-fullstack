package com.timetrak.controller.admin;

import com.timetrak.dto.company.CompanyUpdateDTO;
import com.timetrak.dto.request.DepartmentRequestDTO;
import com.timetrak.dto.company.CompanyResponseDTO;
import com.timetrak.dto.response.DepartmentResponseDTO;
import com.timetrak.service.company.CompanyService;
import com.timetrak.service.department.DepartmentService;
import com.timetrak.service.auth.AuthContextService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/organization")
@Slf4j
@PreAuthorize("hasRole('ADMIN')")

public class OrganizationController {

    private final CompanyService companyService;
    private final DepartmentService departmentService;
    private final AuthContextService authContextService;


    // =============== COMPANY OPERATIONS ===============

    @Operation(summary = "Get company details", description = "Get current admin's company details")
    @GetMapping("/company")
    public ResponseEntity<CompanyResponseDTO> getCompany() {
        CompanyResponseDTO company = companyService.getCompanyDTOById(currentCompanyId());
        return ResponseEntity.ok(company);
    }

    @Operation(summary = "Update company", description = "Update current admin's company details")
    @PatchMapping("/company")
    public ResponseEntity<CompanyResponseDTO> updateCompany(
            @RequestBody CompanyUpdateDTO request
           ) {
        CompanyResponseDTO company = companyService.updateCompany(currentCompanyId(), request);
        return ResponseEntity.ok(company);
    }




    // =============== DEPARTMENT OPERATIONS ===============

    @Operation(summary = "Get all departments", description = "Get all departments in admin's company")
    @GetMapping("/departments")
    public ResponseEntity<Page<DepartmentResponseDTO>> getAllDepartments(
            Pageable pageable) {
        Page<DepartmentResponseDTO> departments = departmentService.getAllByCompanyId(currentCompanyId(), pageable);
        return ResponseEntity.ok(departments);
    }

    @Operation(summary = "Get department by ID", description = "Get specific department by ID")
    @GetMapping("/departments/{id}")
    public ResponseEntity<DepartmentResponseDTO> getDepartmentById(
            @PathVariable Long id) {
        DepartmentResponseDTO department = departmentService.getDepartmentDTOById(id, currentCompanyId());
        return ResponseEntity.ok(department);
    }

    @Operation(summary = "Create department", description = "Create new department in admin's company")
    @PostMapping("/departments")
    public ResponseEntity<DepartmentResponseDTO> createDepartment(
            @RequestBody DepartmentRequestDTO request) {
        DepartmentResponseDTO department = departmentService.addDepartment(request, currentCompanyId());
        return ResponseEntity.ok(department);
    }

    @Operation(summary = "Update department", description = "Update department details")
    @PutMapping("/departments/{id}")
    public ResponseEntity<DepartmentResponseDTO> updateDepartment(
            @PathVariable Long id,
            @RequestBody DepartmentRequestDTO request) {
        DepartmentResponseDTO department = departmentService.updateDepartment(id, currentCompanyId(), request);
        return ResponseEntity.ok(department);
    }

    @Operation(summary = "Delete department", description = "Delete department from admin's company")
    @DeleteMapping("/departments/{id}")
    public ResponseEntity<Map<String, String>> deleteDepartment(
            @PathVariable Long id) {
        log.debug("Request to delete department : {}", id);
        departmentService.deleteDepartment(id, currentCompanyId());
        return ResponseEntity.ok(Map.of("message", "Department deleted successfully"));
    }


    private Long currentCompanyId() {
        return authContextService.getCurrentCompanyId();
    }

} 