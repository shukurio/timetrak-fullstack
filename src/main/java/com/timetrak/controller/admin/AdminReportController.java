package com.timetrak.controller.admin;

import com.timetrak.service.auth.AuthContextService;
import com.timetrak.service.report.ShiftReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {

    private final ShiftReportService shiftReportService;
    private final AuthContextService authContextService;

    @GetMapping("/shifts/company")
    public ResponseEntity<byte[]> exportCompanyShifts(
            @RequestParam(required = false) Integer periodNumber) {

        Long companyId = getCurrentCompanyId();
        byte[] pdfData = shiftReportService.exportShifts(periodNumber, companyId, null);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=company-shifts-report.pdf")
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .body(pdfData);
    }

    @GetMapping("/shifts/departments")
    public ResponseEntity<byte[]> exportDepartmentShifts(
            @RequestParam List<Long> departmentIds,
            @RequestParam(required = false) Integer periodNumber) {

        Long companyId = getCurrentCompanyId();
        byte[] pdfData = shiftReportService.exportShifts(periodNumber, companyId, departmentIds);

        String filename = departmentIds.size() == 1 ?
                "department-shifts-report.pdf" : "multi-department-shifts-report.pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .body(pdfData);
    }

    private Long getCurrentCompanyId() {
        return authContextService.getCurrentCompanyId();
    }
}