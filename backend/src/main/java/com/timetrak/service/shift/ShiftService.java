package com.timetrak.service.shift;

import com.timetrak.dto.shift.ShiftResponseDTO;
import com.timetrak.dto.shift.ShiftSummaryDTO;
import com.timetrak.entity.Employee;
import com.timetrak.entity.Shift;
import com.timetrak.enums.ShiftStatus;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    List<ShiftResponseDTO> getShiftsByDepartmentByDateRange(Long departmentId,
                                                            Long companyId,
                                                            LocalDate startDate,
                                                            LocalDate endDate);

    Map<Long, List<ShiftResponseDTO>> getShiftsByDepartmentsGrouped(
            List<Long> departmentIds,
            Long companyId,
            LocalDate startDate,
            LocalDate endDate);

    Page<ShiftResponseDTO> getShiftsByEmployeeId(Long employeeId, Long companyId, Pageable pageable);
    Page<ShiftResponseDTO> getShiftsByJobTitle(String jobTitle,Long companyId, Pageable pageable);
    Page<ShiftResponseDTO> getShiftsByDateRange(Long companyId,LocalDate startDate, LocalDate endDate, Pageable pageable);
    List<ShiftResponseDTO> getShiftByStatusAndEmployeeIdAndStartDate(ShiftStatus status, Long employeeId,
                                                                     Long companyId, LocalDateTime startDate);
    Page<ShiftResponseDTO> getShiftsByStatusAndPeriodNumber(Integer periodNumber,ShiftStatus status, Long companyId, Pageable pageable);
    Page<ShiftResponseDTO> getShiftsByEmployeeIdAndPeriodNumber(Integer periodNumber, Long employeeId, Long companyId, Pageable pageable);

    Page<ShiftResponseDTO> getThisWeekShifts(Long companyId, Pageable pageable);
    Page<ShiftResponseDTO> getThisMonthShifts(Long companyId,Pageable pageable);

    /**
     * Generates comprehensive shift analytics including hours worked and earnings.
     */
    ShiftSummaryDTO getShiftSummaryForEmployee(Long employeeId, LocalDate startDate, LocalDate endDate);

    ///For CLock out operations only/ no companyId required
    Shift getActiveShiftSelf(Long employeeId);

    ShiftResponseDTO getActiveShift(Long employeeId,Long companyId);



    Page<ShiftResponseDTO> getShiftsByEmployeeIdAndDateRange(Long employeeId,
                                                             LocalDate startDate,
                                                             LocalDate endDate,
                                                             Pageable pageable);

    List<ShiftResponseDTO> getShiftsByEmployeeIdAndDateRange(Long employeeId,
                                                             LocalDate startDate,
                                                             LocalDate endDate);

    Map<Employee, List<ShiftResponseDTO>> getAllShiftsByDateRange(LocalDate startDate, LocalDate endDate, Long companyId);

    Page<ShiftResponseDTO> getShiftsByPeriodNumber(Integer periodNumber, Long companyId, Pageable pageable);
}

