package com.timetrak.controller.user;

import com.timetrak.dto.employee.EmployeeDashboardSummary;
import com.timetrak.service.EmployeeDashboardService;
import com.timetrak.service.auth.AuthContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/employee/dashboard")
public class DashboardController {
    private final AuthContextService authContextService;
    private final EmployeeDashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<EmployeeDashboardSummary> getDashboardSummary() {
        Long employeeId = authContextService.getCurrentEmployeeId();
        Long companyId = authContextService.getCurrentCompanyId();

        EmployeeDashboardSummary summary = dashboardService.getDashboardSummary(employeeId, companyId);
        return ResponseEntity.ok(summary);
    }
}
