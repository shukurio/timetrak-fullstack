package com.timetrak.service.shift;

import com.timetrak.dto.clock.AdminClockRequestDTO;
import com.timetrak.dto.clock.EmployeeClockRequestDTO;
import com.timetrak.dto.response.ClockResponseDTO;
import com.timetrak.dto.response.ShiftResponseDTO;

public interface ClockService {
    ClockResponseDTO adminClockIn(AdminClockRequestDTO request);
    ClockResponseDTO adminClockOut(AdminClockRequestDTO request);
    ShiftResponseDTO employeeClockIn(EmployeeClockRequestDTO request, Long companyId);
    ShiftResponseDTO employeeClockOut(EmployeeClockRequestDTO request, Long companyId);

    ShiftResponseDTO kioskClockIn(EmployeeClockRequestDTO request);
    ShiftResponseDTO kioskClockOut(EmployeeClockRequestDTO request);



    boolean canEmployeeClockIn(Long employeeId);
    boolean canEmployeeClockOut(Long employeeId);

}
