package com.timetrak.controller.user;

import com.timetrak.dto.request.ClockInRequestDTO;
import com.timetrak.dto.request.ClockOutRequestDTO;
import com.timetrak.dto.response.ClockResponseDTO;
import com.timetrak.dto.response.EmployeeJobResponseDTO;
import com.timetrak.dto.response.EmployeeResponseDTO;
import com.timetrak.exception.InvalidOperationException;
import com.timetrak.service.EmployeeJobService;
import com.timetrak.service.employee.EmployeeService;
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
public class UserClockController {
    private final ClockService clockService;
    private final EmployeeJobService employeeJobService;
    private final EmployeeService employeeService;


    @GetMapping("/{username}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeByUsername(@PathVariable String username) {
        EmployeeResponseDTO employee = employeeService.getByUsername(username);
        return ResponseEntity.ok(employee);
    }

    @GetMapping("/determineAction/{employeeId}")
    public String determineAction(@PathVariable Long employeeId) {
        boolean canClockIn = clockService.canEmployeeClockIn(employeeId);
        boolean canClockOut = clockService.canEmployeeClockOut(employeeId);

        log.info("Determining action for employee ID {}: canClockIn={}, canClockOut={}", 
                employeeId, canClockIn, canClockOut);

        if (canClockOut) {
            log.info("Employee {} should CLOCK OUT", employeeId);
            return "ClockOut";
        } else if (canClockIn) {
            log.info("Employee {} should CLOCK IN", employeeId);
            return "ClockIn";
        } else {
            log.warn("Cannot determine action for employee {}: canClockIn={}, canClockOut={}", 
                    employeeId, canClockIn, canClockOut);
            return "Can't determine action";
        }
    }




    @GetMapping("/jobs/{username}")
    public List<EmployeeJobResponseDTO> getAllJobs(@PathVariable String username) {
        EmployeeResponseDTO employee = employeeService.getByUsername(username);
        return employeeJobService.getEmployeeJobs(employee.getId());
    }

    @PostMapping("/clock-in")
    public ResponseEntity<ClockResponseDTO> clockIn(@Valid @NotNull @RequestBody ClockInRequestDTO request) {

        if (request.getEmployeeJobIds().size() != 1) {
            throw new InvalidOperationException("Kiosk mode supports exactly one employee per clock-in request.");
        }

        log.info("Group clock-in request received for {} employees", request.getEmployeeJobIds().size());


        ClockResponseDTO response = clockService.clockIn(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/clock-out")
    public ResponseEntity<ClockResponseDTO> clockOut(@Valid @NotNull @RequestBody ClockOutRequestDTO request) {

        if (request.getEmployeeIds().size() != 1) {
            throw new InvalidOperationException("Kiosk mode supports exactly one employee per clock-out request.");
        }
        log.info("Kiosk clock-out request received for {} employees", request.getEmployeeIds().size());

        ClockResponseDTO response = clockService.clockOut(request);

        log.info("Kiosk clock-out completed: {} successful, {} failed",
                response.getSuccessCount(), response.getFailureCount());

        return ResponseEntity.ok(response);
    }
}
