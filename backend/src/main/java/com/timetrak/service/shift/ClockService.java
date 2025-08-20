package com.timetrak.service.shift;

import com.timetrak.dto.request.ClockInRequestDTO;
import com.timetrak.dto.request.ClockOutRequestDTO;
import com.timetrak.dto.response.ClockResponseDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface ClockService {
    ClockResponseDTO clockIn(@Valid @NotNull ClockInRequestDTO request);
    ClockResponseDTO clockOut(@Valid @NotNull ClockOutRequestDTO request);

    boolean canEmployeeClockIn(Long employeeId);
    boolean canEmployeeClockOut(Long employeeId);

}
