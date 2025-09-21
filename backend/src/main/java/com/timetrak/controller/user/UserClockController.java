package com.timetrak.controller.user;


import com.timetrak.dto.clock.EmployeeClockRequestDTO;
import com.timetrak.dto.employeeJob.EmployeeJobResponseDTO;
import com.timetrak.dto.response.ShiftResponseDTO;
import com.timetrak.enums.ClockAction;
import com.timetrak.service.auth.AuthContextService;
import com.timetrak.service.employeeJob.EmployeeJobQueryService;
import com.timetrak.service.clock.ClockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee/clock")
@RequiredArgsConstructor
@Slf4j
public class UserClockController {
    private final ClockService clockService;
    private final AuthContextService authContextService;
    private final EmployeeJobQueryService empJobService;

    @GetMapping("/determineAction/{employeeId}")
    public ResponseEntity<ClockAction> determineAction(@PathVariable Long employeeId) {
        ClockAction action = clockService.determineAction(employeeId);
        return ResponseEntity.ok(action);
    }

    @GetMapping("jobs")
    public ResponseEntity<List<EmployeeJobResponseDTO>> getAllJobsForEmployee() {
        List<EmployeeJobResponseDTO> jobs = empJobService.getEmployeeJobs(employeeId(),companyId());
        return ResponseEntity.ok(jobs);
    }

    @PostMapping("/clockIn")
    public ResponseEntity<ShiftResponseDTO> clockIn(@RequestBody EmployeeClockRequestDTO request) {
        //frontend passes empJobId
        ShiftResponseDTO newShift = clockService.employeeClockIn(request,companyId());
        return ResponseEntity.ok(newShift);
    }

    @PostMapping("/clockOut")
    public ResponseEntity<ShiftResponseDTO> clockOut(@RequestBody EmployeeClockRequestDTO request) {
        ShiftResponseDTO newShift = clockService.employeeClockOut(request,companyId());
        return ResponseEntity.ok(newShift);
    }


    private Long companyId(){
        return authContextService.getCurrentCompanyId();
    }
    private Long employeeId(){
        return authContextService.getCurrentEmployeeId();
    }
}
