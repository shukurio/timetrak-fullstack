package com.timetrak.controller.user;


import com.timetrak.dto.clock.EmployeeClockRequestDTO;
import com.timetrak.dto.response.ShiftResponseDTO;
import com.timetrak.service.auth.AuthContextService;
import com.timetrak.service.shift.ClockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employee/clock")
@RequiredArgsConstructor
@Slf4j
public class UserClockController {
    private final ClockService clockService;
    private final AuthContextService authContextService;


    public ResponseEntity<ShiftResponseDTO> clockIn(EmployeeClockRequestDTO request) {
        //frontend passes empJobId
        ShiftResponseDTO newShift = clockService.employeeClockIn(request,companyId());
        return ResponseEntity.ok(newShift);
    }

    public ResponseEntity<ShiftResponseDTO> clockOut(EmployeeClockRequestDTO request) {
        ShiftResponseDTO newShift = clockService.employeeClockOut(request,companyId());
        return ResponseEntity.ok(newShift);
    }


    private Long companyId(){
        return authContextService.getCurrentCompanyId();
    }
}
