package com.timetrak.controller.admin;

import com.timetrak.dto.employeeJob.*;
import com.timetrak.service.auth.AuthContextService;
import com.timetrak.service.employeeJob.EmployeeJobManagementService;
import com.timetrak.service.employeeJob.EmployeeJobQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/employee-jobs")
@Validated
public class EmployeeJobController {
    
    private final EmployeeJobManagementService managementService;
    private final EmployeeJobQueryService queryService;
    private final AuthContextService authContextService;

    @PostMapping("/assign")
    public ResponseEntity<EmployeeJobBulkResponseDTO> assignJobToEmployees(@Valid @RequestBody EmployeeJobRequestDTO request) {
        log.info("Bulk assigning job {} to {} employees", request.getJobId(), request.getEmployeeIds().size());
        EmployeeJobBulkResponseDTO response = managementService.assignJobToEmployees(request, currentCompanyId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/remove")
    public ResponseEntity<EmployeeJobBulkResponseDTO> removeJobFromEmployees(
            @RequestParam List<Long> employeeIds,
            @RequestParam Long jobId) {
        log.info("Bulk removing job {} from {} employees", jobId, employeeIds.size());
        EmployeeJobBulkResponseDTO response = managementService.removeJobFromEmployees(employeeIds, jobId, currentCompanyId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/individual")
    public ResponseEntity<EmployeeJobResponseDTO> createIndividualAssignment(
            @RequestParam Long employeeId,
            @RequestParam Long jobId,
            @RequestParam(required = false) java.math.BigDecimal hourlyWage) {
        EmployeeJobResponseDTO assignment = managementService.createAssignment(employeeId, jobId, hourlyWage, currentCompanyId());
        return ResponseEntity.status(HttpStatus.CREATED).body(assignment);
    }

    @DeleteMapping("/{employeeJobId}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long employeeJobId) {
        managementService.deleteAssignment(employeeJobId, currentCompanyId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{employeeJobId}")
    public ResponseEntity<EmployeeJobResponseDTO> updateAssignment(
            @PathVariable Long employeeJobId,
            @Valid @RequestBody EmployeeJobUpdateDTO request) {
        EmployeeJobResponseDTO updated = managementService.updateAssignment(employeeJobId, request, currentCompanyId());
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    public ResponseEntity<List<EmployeeJobResponseDTO>> getAllAssignments() {
        List<EmployeeJobResponseDTO> assignments = queryService.getAllAssignments(currentCompanyId());
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<EmployeeJobResponseDTO>> getEmployeeJobs(@PathVariable Long employeeId) {
        List<EmployeeJobResponseDTO> jobs = queryService.getEmployeeJobs(employeeId, currentCompanyId());
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<EmployeeJobResponseDTO>> getJobAssignments(@PathVariable Long jobId) {
        List<EmployeeJobResponseDTO> assignments = queryService.getJobAssignments(jobId, currentCompanyId());
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<EmployeeJobResponseDTO>> getDepartmentAssignments(@PathVariable Long departmentId) {
        List<EmployeeJobResponseDTO> assignments = queryService.getDepartmentAssignments(departmentId, currentCompanyId());
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/employee/username/{username}")
    public ResponseEntity<List<EmployeeJobResponseDTO>> getEmployeeJobsByUsername(@PathVariable String username) {
        List<EmployeeJobResponseDTO> jobs = queryService.getEmployeeJobsByUsername(username, currentCompanyId());
        return ResponseEntity.ok(jobs);
    }

    private Long currentCompanyId() {
        return authContextService.getCurrentCompanyId();
    }
}
