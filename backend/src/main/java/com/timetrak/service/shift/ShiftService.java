package com.timetrak.service.shift;

import com.timetrak.dto.response.ShiftResponseDTO;
import com.timetrak.dto.response.ShiftSummaryDTO;
import com.timetrak.entity.Employee;
import com.timetrak.entity.Shift;
import com.timetrak.enums.ShiftStatus;
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

    ShiftResponseDTO getShiftById(@NotNull Long shiftId);



    Page<ShiftResponseDTO> getShiftsByDepartment(Long departmentId,Long companyId,Pageable pageable);
    Page<ShiftResponseDTO> getShiftsFromDate(LocalDate startDate, Long companyId, Pageable pageable);
    Page<ShiftResponseDTO> getShiftsByEmployeeId(Long employeeId,Long companyId, Pageable pageable);
    Page<ShiftResponseDTO> getShiftsByJobTitle(String jobTitle,Long companyId, Pageable pageable);
    Page<ShiftResponseDTO> getShiftsByDateRange(Long companyId,LocalDate startDate, LocalDate endDate, Pageable pageable);
    Page<ShiftResponseDTO> getShiftByStatusAndEmployeeId(ShiftStatus status,Long employeeId,
                                                         Long companyId,
                                                         Pageable pageable);
    Page<ShiftResponseDTO> getShiftsByStatus(ShiftStatus status,Long companyId, Pageable pageable);

    //Internal Use
    List<ShiftResponseDTO> getShiftsByDateRange(Long companyId,LocalDate startDate, LocalDate endDate);


    Page<ShiftResponseDTO> getTodaysShifts(Long companyId,Pageable pageable);
    Page<ShiftResponseDTO> getThisWeekShifts(Long companyId, Pageable pageable);
    Page<ShiftResponseDTO> getThisMonthShifts(Long companyId,Pageable pageable);

    /**
     * Generates comprehensive shift analytics including hours worked and earnings.
     */
    ShiftSummaryDTO getShiftSummaryForEmployee(Long employeeId, LocalDate startDate, LocalDate endDate);

    ///For CLock out operations only/ no companyId required
    Shift getActiveShift(Long employeeId);


    Page<ShiftResponseDTO> getShiftsByEmployeeIdAndDateRange(Long employeeId,
                                                             LocalDate startDate,
                                                             LocalDate endDate,
                                                             Pageable pageable);

    List<ShiftResponseDTO> getShiftsByEmployeeIdAndDateRange(Long employeeId,
                                                             LocalDate startDate,
                                                             LocalDate endDate);

    Map<Employee, List<ShiftResponseDTO>> getAllShiftsByDateRange(LocalDate startDate, LocalDate endDate, Long companyId);
}

