package com.timetrak.service;

import com.timetrak.dto.request.*;
import com.timetrak.dto.response.ClockResponseDTO;
import com.timetrak.dto.response.ShiftResponseDTO;
import com.timetrak.dto.response.ShiftSummaryDTO;
import com.timetrak.entity.Shift;
import com.timetrak.enums.JobTitle;
import com.timetrak.enums.ShiftStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;


/**
 * Service interface for managing employee shifts, clock operations, and time tracking.
 * Provides comprehensive shift management including individual/group operations and analytics.
 */
@Validated
public interface ShiftService {

    ShiftResponseDTO createShift(@Valid @NotNull ShiftRequestDTO request);
    ShiftResponseDTO updateShift(@NotNull Long shiftId, @Valid @NotNull ShiftRequestDTO request);
    Shift getShiftById(@NotNull Long shiftId);
    void deleteShift(@NotNull Long shiftId);


    /**
     * Bulk clock operations for administrative use.
     */
    ClockResponseDTO clockIn(@Valid @NotNull ClockInRequestDTO request);
    ClockResponseDTO clockOut(@Valid @NotNull ClockOutRequestDTO request);

    boolean canEmployeeClockIn(Long employeeId);
    boolean canEmployeeClockOut(Long employeeId);

    Page<ShiftResponseDTO> getAllShifts(Pageable pageable);
    Page<ShiftResponseDTO> getShiftsFromDate(LocalDate startDate, Pageable pageable);
    Page<ShiftResponseDTO> getShiftsByEmployeeId(Long employeeId, Pageable pageable);
    Page<ShiftResponseDTO> getShiftsByJobTitle(JobTitle jobTitle, Pageable pageable);
    Page<ShiftResponseDTO> getShiftsByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable);
    Page<ShiftResponseDTO> getShiftByStatusAndEmployeeId(Long employeeId, ShiftStatus status, Pageable pageable);
    Page<ShiftResponseDTO> getShiftsByStatus(ShiftStatus status, Pageable pageable);


    Page<ShiftResponseDTO> getTodaysShifts(Pageable pageable);
    Page<ShiftResponseDTO> getThisWeekShifts(Pageable pageable);
    Page<ShiftResponseDTO> getThisMonthShifts(Pageable pageable);

    /**
     * Generates comprehensive shift analytics including hours worked and earnings.
     */
    ShiftSummaryDTO getShiftSummary(Long employeeId, LocalDate startDate, LocalDate endDate);

    Shift getActiveShift(@NotNull Long employeeId);


    Page<ShiftResponseDTO> getShiftsByEmployeeIdAndDateRange(Long employeeId,
                                                             LocalDate startDate,
                                                             LocalDate endDate,
                                                             Pageable pageable);

    List<ShiftResponseDTO> getShiftsByEmployeeIdAndDateRange(Long employeeId,
                                                             LocalDate startDate,
                                                             LocalDate endDate);

    Map<Long, List<ShiftResponseDTO>> getAllShiftsByDateRange(LocalDate startDate, LocalDate endDate, Long companyId);
}

