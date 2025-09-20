package com.timetrak.controller.unsecured;

import com.timetrak.dto.clock.EmployeeClockRequestDTO;
import com.timetrak.dto.employeeJob.EmployeeJobResponseDTO;
import com.timetrak.dto.response.EmployeeResponseDTO;
import com.timetrak.dto.response.ShiftResponseDTO;
import com.timetrak.enums.ClockAction;
import com.timetrak.service.employee.EmployeeService;
import com.timetrak.service.employeeJob.EmployeeJobQueryService;
import com.timetrak.service.shift.ClockService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/kiosk")
public class KioskController {
    private final ClockService clockService;
    private final EmployeeService employeeService;
    private final EmployeeJobQueryService empJobService;


    @GetMapping("/{username}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeByUsername(@PathVariable String username) {
        EmployeeResponseDTO employee = employeeService.getByUsername(username);
        return ResponseEntity.ok(employee);
    }

    @GetMapping("/determineAction/{employeeId}")
    public ResponseEntity<ClockAction> determineAction(@PathVariable Long employeeId) {
        ClockAction action = clockService.determineAction(employeeId);
        return ResponseEntity.ok(action);
    }

    @GetMapping("/jobs/{username}")
    public List<EmployeeJobResponseDTO> getAllJobs(@PathVariable String username) {
        EmployeeResponseDTO employee = employeeService.getByUsername(username);
        return empJobService.getEmployeeJobs(employee.getId(),
                // only for kiosk just to go around it
                employee.getCompanyId());
    }

    @PostMapping("/clock-in")
    public ResponseEntity<ShiftResponseDTO> clockIn(@Valid @NotNull @RequestBody EmployeeClockRequestDTO request) {


        ShiftResponseDTO response = clockService.kioskClockIn(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/clock-out")
    public ResponseEntity<ShiftResponseDTO> clockOut(@Valid @NotNull @RequestBody EmployeeClockRequestDTO request) {

        ShiftResponseDTO response = clockService.kioskClockOut(request);


        return ResponseEntity.ok(response);
    }
}
