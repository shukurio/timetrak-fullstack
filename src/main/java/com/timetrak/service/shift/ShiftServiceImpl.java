package com.timetrak.service.shift;

import com.timetrak.dto.payment.Period;
import com.timetrak.dto.response.*;
import com.timetrak.entity.Employee;
import com.timetrak.entity.Shift;
import com.timetrak.enums.ClockErrorCode;
import com.timetrak.enums.ShiftStatus;
import com.timetrak.exception.ResourceNotFoundException;
import com.timetrak.mapper.ShiftMapper;
import com.timetrak.repository.ShiftRepository;
import com.timetrak.service.employee.EmployeeService;
import com.timetrak.service.payment.PeriodService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.timetrak.constant.ShiftConstants.PRECISION_FACTOR;

@RequiredArgsConstructor
@Slf4j
@Service
@Validated
public class ShiftServiceImpl implements ShiftService {

    private final ShiftRepository shiftRepository;
    private final ShiftMapper shiftMapper;
    private final EmployeeService employeeService;
    private final PeriodService periodService;

    @Override
    public ShiftResponseDTO getShiftById(Long shiftId) {
        Objects.requireNonNull(shiftId, "Shift ID cannot be null");

        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(ClockErrorCode.SHIFT_NOT_FOUND.getDefaultMessage() + " with id: " + shiftId));

        return shiftMapper.toDTO(shift);
    }


    @Override
    public Page<ShiftResponseDTO> getShiftsByDepartment(Long departmentId, Long companyId, Pageable pageable) {
        return shiftRepository.findByDepartmentIdAndCompanyId(departmentId,companyId,pageable)
                .map(shiftMapper::toDTO);
    }

    @Override
    public List<ShiftResponseDTO> getShiftsByDepartmentByDateRange(Long departmentId,
                                                                   Long companyId,
                                                                   LocalDate startDate,
                                                                   LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        return shiftRepository.findShiftsForDepartmentDateRange(departmentId,
                        companyId,
                        startDateTime,
                        endDateTime)
                .stream().map(shiftMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public Map<Long, List<ShiftResponseDTO>> getShiftsByDepartmentsGrouped(
            List<Long> departmentIds,
            Long companyId,
            LocalDate startDate,
            LocalDate endDate) {



        Map<Long, List<ShiftResponseDTO>> result = new LinkedHashMap<>();

        for (Long departmentId : departmentIds) {
            List<ShiftResponseDTO> shifts = getShiftsByDepartmentByDateRange(
                    departmentId, companyId, startDate, endDate);

            if (!shifts.isEmpty()) {
                result.put(departmentId, shifts);
            }
        }

        return result;
    }



    @Override
    public Page<ShiftResponseDTO> getShiftsByEmployeeId(Long employeeId,Long companyId,  Pageable pageable) {
        return shiftRepository.findByEmployeeId(employeeId,companyId, pageable).map(shiftMapper::toDTO);
    }


    @Override
    public Page<ShiftResponseDTO> getShiftsByJobTitle(String jobTitle,Long companyId, Pageable pageable) {
        return shiftRepository.findByJobTitle(jobTitle, companyId, pageable).map(shiftMapper::toDTO);
    }

    @Override
    public Page<ShiftResponseDTO> getShiftsByDateRange(Long companyId,
                                                       LocalDate startDate,
                                                       LocalDate endDate,
                                                       Pageable pageable) {
        LocalDateTime startDateTime = toStartOfDay(startDate);
        LocalDateTime endDateTime = toEndOfDay(endDate);
        Page<Shift> shifts = shiftRepository.findByCompanyIdAndDateRange(companyId,startDateTime, endDateTime, pageable);
        return shifts.map(shiftMapper::toDTO);
    }

    @Override
    public Page<ShiftResponseDTO> getShiftsByEmployeeIdAndDateRange(Long employeeId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        LocalDateTime startDateTime = toStartOfDay(startDate);
        LocalDateTime endDateTime = toEndOfDay(endDate);
        Page<Shift> shifts = shiftRepository.findByEmployeeIdAndDateRange(employeeId, startDateTime, endDateTime, pageable);
        return shifts.map(shiftMapper::toDTO);
    }

    @Override
    public List<ShiftResponseDTO> getShiftsByEmployeeIdAndDateRange(Long employeeId, LocalDate startDate, LocalDate endDate) {
       return shiftRepository.findByEmployeeIdAndDateRange(employeeId, startDate.atStartOfDay(), endDate.atTime(23, 59, 59))
               .stream().map(shiftMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public Page<ShiftResponseDTO> getShiftByStatusAndEmployeeId(ShiftStatus status,Long employeeId,
                                                                Long companyId,
                                                                Pageable pageable) {
        return shiftRepository.findByStatusAndEmployeeIdAndCompanyId(
                status, employeeId,companyId, pageable).map(shiftMapper::toDTO);
    }

    @Override
    public Page<ShiftResponseDTO> getShiftsByStatusAndPeriodNumber(Integer periodNumber,ShiftStatus status, Long companyId, Pageable pageable) {

        Period period = periodService.getPeriodByNumber(periodNumber, companyId);


        return shiftRepository.findAllByStatusAndDateRangeAndCompanyId(status,
                period.getStartDate(),
                period.getEndDate(),
                companyId,
                pageable).map(shiftMapper::toDTO);
    }

    @Override
    public Page<ShiftResponseDTO> getShiftsByEmployeeIdAndPeriodNumber(Integer periodNumber,
                                                                       Long employeeId,
                                                                       Long companyId, Pageable pageable) {

        Period period = periodService.getPeriodByNumber(periodNumber, companyId);

        return getShiftsByEmployeeIdAndDateRange(employeeId, period.getStartDate(), period.getEndDate(), pageable);


    }


    @Override
    public List<ShiftResponseDTO> getShiftsByDateRange(Long companyId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = toStartOfDay(startDate);
        LocalDateTime endDateTime = toEndOfDay(endDate);
        List<Shift> shifts = shiftRepository.findByCompanyIdAndDateRange(companyId,startDateTime,endDateTime);
        return shiftMapper.toDTOList(shifts);
    }

    @Override
    public Page<ShiftResponseDTO> getShiftsFromDate(LocalDate startDate,Long companyId, Pageable pageable) {
        LocalDateTime startDateTime = toStartOfDay(startDate);
        return shiftRepository.findByDateFrom(startDateTime,companyId, pageable).map(shiftMapper::toDTO);
    }

    @Override
    public Page<ShiftResponseDTO> getTodaysShifts(Long companyId, Pageable pageable) {
        return getShiftsFromDate(LocalDate.now(),companyId, pageable);
    }

    @Override
    public Page<ShiftResponseDTO> getThisWeekShifts(Long companyId,Pageable pageable) {
        return getShiftsFromDate(LocalDate.now().with(DayOfWeek.MONDAY),companyId, pageable);
    }

    @Override
    public Page<ShiftResponseDTO> getThisMonthShifts(Long companyId,Pageable pageable) {
        return getShiftsFromDate(LocalDate.now().withDayOfMonth(1),companyId, pageable);
    }



    @Override
    public ShiftSummaryDTO getShiftSummaryForEmployee(Long employeeId, LocalDate startDate, LocalDate endDate) {
        Page<Shift> shifts = shiftRepository.findByEmployeeIdAndDateRange(
                employeeId,
                toStartOfDay(startDate),
                toEndOfDay(endDate),
                Pageable.unpaged()
        );

        List<Shift> shiftList = shifts.getContent();
        int totalShifts = shiftList.size();
        int activeShifts = (int) shiftList.stream().filter(s -> s.getStatus() == ShiftStatus.ACTIVE).count();
        int completedShifts = (int) shiftList.stream().filter(s -> s.getStatus() == ShiftStatus.COMPLETED).count();

        double totalHours = shiftList.stream()
                .filter(s -> s.getStatus() == ShiftStatus.COMPLETED && s.getClockOut() != null)
                .mapToDouble(s -> Duration.between(s.getClockIn(), s.getClockOut()).toMinutes() / 60.0)
                .sum();

        double totalEarnings = shiftList.stream()
                .filter(s -> s.getStatus() == ShiftStatus.COMPLETED && s.getClockOut() != null)
                .mapToDouble(s -> {
                    double hours = Duration.between(s.getClockIn(), s.getClockOut()).toMinutes() / 60.0;
                    return hours * s.getEmployeeJob().getHourlyWage().doubleValue();
                })
                .sum();

        String employeeName = employeeService.getEmployeeNameById(employeeId);
        double averageShiftLength = completedShifts > 0 ? totalHours / completedShifts : 0.0;

        return ShiftSummaryDTO.builder()
                .employeeId(employeeId)
                .employeeName(employeeName)
                .startDate(startDate)
                .endDate(endDate)
                .totalShifts(totalShifts)
                .activeShifts(activeShifts)
                .completedShifts(completedShifts)
                .totalHours(roundToPrecision(totalHours))
                .totalEarnings(roundToPrecision(totalEarnings))
                .averageShiftLength(roundToPrecision(averageShiftLength))
                .build();
    }

    ///For CLock out operations only/ no companyId required
    @Override
    public Shift getActiveShiftSelf(Long employeeId) {
        return shiftRepository.findActiveShiftByEmployeeId(employeeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(ClockErrorCode.NO_ACTIVE_SHIFT.getDefaultMessage()
                                + " for employee ID: " + employeeId));
    }

    @Override
    public Shift getActiveShift(Long employeeId, Long companyId) {
        return shiftRepository.findActiveShiftByEmployeeId(employeeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(ClockErrorCode.NO_ACTIVE_SHIFT.getDefaultMessage()
                                + " for employee ID: " + employeeId));    }

    @Override
    public Map<Employee, List<ShiftResponseDTO>> getAllShiftsByDateRange(
             LocalDate startDate, LocalDate endDate, Long companyId) {
        // Single batch query
        List<Shift> shifts = shiftRepository.findAllByCompanyIdAndDateRange(
                startDate, endDate, companyId);

        // Group by empID and convert to DTOs
        // Note: Query already filters out null employees, so no additional filtering needed
        return shifts.stream()
                .collect(Collectors.groupingBy(
                        Shift::getEmployee,
                        Collectors.mapping(shiftMapper::toDTO, Collectors.toList())
                ));
    }

    @Override
    public Page<ShiftResponseDTO> getShiftsByPeriodNumber(Integer periodNumber, Long companyId, Pageable pageable) {

        Period period = periodService.getPeriodByNumber(periodNumber, companyId);
        if(period == null) {
            period = periodService.getCurrentPeriod(companyId);
        }
        //TODO validation
        return getShiftsByDateRange(companyId,period.getStartDate(),period.getEndDate(),pageable);
    }

    /**
     * Rounds a double value to 2 decimal places for consistent precision
     */
    private double roundToPrecision(double value) {
        return Math.round(value * PRECISION_FACTOR) / PRECISION_FACTOR;
    }

    /**
     * Converts LocalDate to start of day LocalDateTime
     */
    private LocalDateTime toStartOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    /**
     * Converts LocalDate to end of day LocalDateTime
     */
    private LocalDateTime toEndOfDay(LocalDate date) {
        return date.atTime(23, 59, 59);
    }


}
