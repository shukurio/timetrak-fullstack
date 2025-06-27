package com.timetrak.controller;

import com.timetrak.dto.request.CompanyRequestDTO;
import com.timetrak.dto.request.DepartmentRequestDTO;
import com.timetrak.dto.response.CompanyResponseDTO;
import com.timetrak.dto.response.DepartmentResponseDTO;
import com.timetrak.security.auth.CustomUserDetails;
import com.timetrak.service.CompanyService;
import com.timetrak.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/organization")
@PreAuthorize("hasRole('ADMIN')")
public class OrganizationController {

    private final CompanyService companyService;
    private final DepartmentService departmentService;

    @ModelAttribute("companyId")
    public Long getCompanyId(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return userDetails.getCompanyId();
    }

    // =============== COMPANY OPERATIONS ===============

    @Operation(summary = "Get company details", description = "Get current admin's company details")
    @GetMapping("/company")
    public ResponseEntity<CompanyResponseDTO> getCompany(@ModelAttribute("companyId") Long companyId) {
        CompanyResponseDTO company = companyService.getCompanyDTOById(companyId);
        return ResponseEntity.ok(company);
    }

    @Operation(summary = "Update company", description = "Update current admin's company details")
    @PutMapping("/company")
    public ResponseEntity<CompanyResponseDTO> updateCompany(
            @RequestBody CompanyRequestDTO request,
            @ModelAttribute("companyId") Long companyId) {
        CompanyResponseDTO company = companyService.updateCompany(companyId, request);
        return ResponseEntity.ok(company);
    }




    // =============== DEPARTMENT OPERATIONS ===============

    @Operation(summary = "Get all departments", description = "Get all departments in admin's company")
    @GetMapping("/departments")
    public ResponseEntity<Page<DepartmentResponseDTO>> getAllDepartments(
            Pageable pageable,
            @ModelAttribute("companyId") Long companyId) {
        Page<DepartmentResponseDTO> departments = departmentService.getAllByCompanyId(companyId, pageable);
        return ResponseEntity.ok(departments);
    }

    @Operation(summary = "Get department by ID", description = "Get specific department by ID")
    @GetMapping("/departments/{id}")
    public ResponseEntity<DepartmentResponseDTO> getDepartmentById(
            @PathVariable Long id,
            @ModelAttribute("companyId") Long companyId) {
        DepartmentResponseDTO department = departmentService.getDepartmentDTOById(id, companyId);
        return ResponseEntity.ok(department);
    }

    @Operation(summary = "Create department", description = "Create new department in admin's company")
    @PostMapping("/departments")
    public ResponseEntity<DepartmentResponseDTO> createDepartment(
            @RequestBody DepartmentRequestDTO request,
            @ModelAttribute("companyId") Long companyId) {
        DepartmentResponseDTO department = departmentService.addDepartment(request, companyId);
        return ResponseEntity.ok(department);
    }

    @Operation(summary = "Update department", description = "Update department details")
    @PutMapping("/departments/{id}")
    public ResponseEntity<DepartmentResponseDTO> updateDepartment(
            @PathVariable Long id,
            @RequestBody DepartmentRequestDTO request,
            @ModelAttribute("companyId") Long companyId) {
        DepartmentResponseDTO department = departmentService.updateDepartment(id, companyId, request);
        return ResponseEntity.ok(department);
    }

    @Operation(summary = "Delete department", description = "Delete department from admin's company")
    @DeleteMapping("/departments/{id}")
    public ResponseEntity<Map<String, String>> deleteDepartment(
            @PathVariable Long id,
            @ModelAttribute("companyId") Long companyId) {
        departmentService.deleteDepartment(id, companyId);
        return ResponseEntity.ok(Map.of("message", "Department deleted successfully"));
    }

    @Operation(summary = "Get department by code", description = "Get department by department code")
    @GetMapping("/departments/code/{code}")
    public ResponseEntity<DepartmentResponseDTO> getDepartmentByCode(
            @PathVariable String code,
            @ModelAttribute("companyId") Long companyId) {
        DepartmentResponseDTO department = departmentService.getDepartmentByCode(code, companyId);
        return ResponseEntity.ok(department);
    }
} 