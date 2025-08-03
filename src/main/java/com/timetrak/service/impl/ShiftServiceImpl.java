package com.timetrak.service.impl;

import com.timetrak.dto.request.*;
import com.timetrak.dto.response.*;
import com.timetrak.entity.EmployeeJob;
import com.timetrak.entity.Shift;
import com.timetrak.enums.ClockErrorCode;
import com.timetrak.enums.ClockOperationType;
import com.timetrak.enums.JobTitle;
import com.timetrak.enums.ShiftStatus;
import com.timetrak.exception.InvalidOperationException;
import com.timetrak.exception.ResourceNotFoundException;
import com.timetrak.mapper.ShiftMapper;
import com.timetrak.repository.ShiftRepository;
import com.timetrak.service.EmployeeJobService;
import com.timetrak.service.employee.EmployeeService;
import com.timetrak.service.ShiftService;
import jakarta.transaction.Transactional;
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

@RequiredArgsConstructor
@Slf4j
@Service
@Validated
public class ShiftServiceImpl implements ShiftService {

    private static final double PRECISION_FACTOR = 100.0;
    private static final int MAX_GROUP_OPERATION_SIZE = 100;
    private static final int MAX_SHIFT_DURATION_HOURS = 24;
    private static final int MAX_NOTES_LENGTH = 1000;

    // Clock-in validation messages
    private static final String EMPTY_EMPLOYEE_JOB_IDS_MSG = "Employee job IDs list cannot be empty";
    private static final String CLOCK_IN_FUTURE_TIME_MSG = "Clock in time cannot be in the future";

    // Clock-out validation messages
    private static final String EMPTY_EMPLOYEE_IDS_MSG = "Employee IDs list cannot be empty";
    private static final String CLOCK_OUT_FUTURE_TIME_MSG = "Clock out time cannot be in the future";

    // Shared messages
    private static final String OPERATION_SIZE_EXCEEDED_MSG = "Group operation size exceeds maximum allowed: " + MAX_GROUP_OPERATION_SIZE;

    private final ShiftRepository shiftRepository;
    private final ShiftMapper shiftMapper;
    private final EmployeeJobService employeeJobService;
    private final EmployeeService employeeService;

    @Override
    @Transactional
    public ShiftResponseDTO createShift(ShiftRequestDTO request) {
        validateShiftRequest(request);

        Shift shift = shiftMapper.toEntity(request);
        Shift savedShift = shiftRepository.save(shift);

        log.info("Created shift {} for employee job {}", savedShift.getId(), request.getEmployeeJobId());
        return shiftMapper.toDTO(savedShift);
    }

    @Override
    @Transactional
    public ShiftResponseDTO updateShift(Long shiftId, ShiftRequestDTO request) {
        validateShiftRequest(request);

        Shift shift = getShiftById(shiftId);
        validateShiftUpdatePermissions(shift);

        shiftMapper.updateShiftFromDto(request, shift);
        Shift updatedShift = shiftRepository.save(shift);

        log.info("Updated shift {} for employee job {}", shiftId, request.getEmployeeJobId());
        return shiftMapper.toDTO(updatedShift);
    }

    @Override
    public Shift getShiftById(Long shiftId) {
        Objects.requireNonNull(shiftId, "Shift ID cannot be null");

        return shiftRepository.findById(shiftId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(ClockErrorCode.SHIFT_NOT_FOUND.getDefaultMessage() + " with id: " + shiftId));
    }

    @Override
    @Transactional
    public void deleteShift(Long id) {
        Objects.requireNonNull(id, "Shift ID cannot be null");

        Shift shift = getShiftById(id);
        validateShiftDeletionPermissions(shift);
        shift.markAsDeleted();
        shiftRepository.save(shift);

        log.info("Soft deleted shift {}", id);
    }

    @Override
    @Transactional
    public ClockResponseDTO clockIn(ClockInRequestDTO request) {
        validateClockInRequest(request);

        log.info("Processing group clock-in for {} employees", request.getEmployeeJobIds().size());

        List<EmployeeJobInfoDTO> employeeJobs = employeeJobService.getEmpJobsInfoByIds(request.getEmployeeJobIds());
        List<ClockFailureResponse> failed = new ArrayList<>();
        List<Shift> shiftsToSave = new ArrayList<>();

        for (EmployeeJobInfoDTO empJob : employeeJobs) {
            try {
                if (!canEmployeeClockIn(empJob.getEmployeeId())) {
                    failed.add(buildClockFailure(empJob, ClockErrorCode.ALREADY_CLOCKED_IN.getDefaultMessage(), ClockErrorCode.ALREADY_CLOCKED_IN));
                    continue;
                }

                EmployeeJob employeeJob = employeeJobService.getEmpJobById(empJob.getEmployeeJobId());
                validateEmployeeJobActive(employeeJob);

                Shift shift = Shift.builder()
                        .clockIn(request.getClockInTime() != null ? request.getClockInTime() : LocalDateTime.now())
                        .employeeJob(employeeJob)
                        .employeeId(empJob.getEmployeeId())
                        .companyId(empJob.getCompanyId())
                        .status(ShiftStatus.ACTIVE)
                        .notes(sanitizeNotes(request.getNotes()))
                        .build();




                shiftsToSave.add(shift);
                                log.info("Employee {} successfully clocked in for job {} at {}",
                        employeeJob.getEmployee().getUsername(),
                        employeeJob.getJob().getJobTitle(),
                        shift.getClockIn());

            } catch (Exception e) {
                log.error("Failed to clock in employee: {} - {}", empJob.getFullName(), e.getMessage(), e);
                failed.add(buildClockFailure(empJob, e.getMessage(), ClockErrorCode.CLOCK_IN_ERROR));
            }
        }


        List<ShiftResponseDTO> successful = new ArrayList<>(shiftMapper.toDTOList(shiftRepository.saveAll(shiftsToSave)));
        log.info("Group clock-in completed: {} successful, {} failed", successful.size(), failed.size());

        ClockResponseDTO response = ClockResponseDTO.builder()
                .totalProcessed(employeeJobs.size())
                .successCount(successful.size())
                .failureCount(failed.size())
                .operationTime(LocalDateTime.now())
                .operationType(ClockOperationType.CLOCK_IN.name())
                .successful(successful)
                .failed(failed)
                .build();

        if (response.isCompletelySuccessful()) {
            log.info("Clock-in operation COMPLETELY SUCCESSFUL: All {} employees clocked in",
                    response.getSuccessCount());
        } else if (response.isMixedResult()) {
            log.warn("Clock-in operation had PARTIAL FAILURES: {} succeeded, {} failed",
                    response.getSuccessCount(), response.getFailureCount());
        } else {
            log.error("Clock-in operation COMPLETELY FAILED: 0 succeeded, {} failed",
                    response.getFailureCount());
        }
        return response;
    }

    @Override
    @Transactional
    public ClockResponseDTO clockOut(ClockOutRequestDTO request) {
        validateClockOutRequest(request);

        log.info("Processing group clock-out for {} employees", request.getEmployeeIds().size());

        List<ClockFailureResponse> failed = new ArrayList<>();

        List<Shift> shiftsToSave = new ArrayList<>();

        for (Long employeeId : request.getEmployeeIds()) {
            try {
                if (!canEmployeeClockOut(employeeId)) {
                    String employeeName = getEmployeeNameSafely(employeeId);
                    failed.add(buildClockOutFailure(employeeId, employeeName, ClockErrorCode.NO_ACTIVE_SHIFT.getDefaultMessage(), ClockErrorCode.NO_ACTIVE_SHIFT));
                    continue;
                }


        Shift shift = getActiveShift(employeeId);
        LocalDateTime clockOutTime = request.getClockOutTime() != null ? request.getClockOutTime() : LocalDateTime.now();

        validateClockOutTime(shift.getClockIn(), clockOutTime);

        shift.setClockOut(clockOutTime);

        String newNotes = sanitizeNotes(request.getNotes());
        if (newNotes != null && !newNotes.trim().isEmpty()) {
            String existingNotes = shift.getNotes() != null ? shift.getNotes() : "";
            String combinedNotes = existingNotes.isEmpty() ? newNotes : existingNotes + "\n" + newNotes;
            shift.setNotes(truncateNotes(combinedNotes));
        }

        shift.setStatus(ShiftStatus.COMPLETED);

        shiftsToSave.add(shift);

            } catch (Exception e) {
                log.error("Failed to clock out employee ID: {} - {}", employeeId, e.getMessage(), e);
                String employeeName = getEmployeeNameSafely(employeeId);
                failed.add(buildClockOutFailure(employeeId, employeeName, e.getMessage(), ClockErrorCode.CLOCK_OUT_ERROR));
            }
        }

        List<ShiftResponseDTO> successful = new ArrayList<>(shiftMapper.toDTOList(shiftRepository.saveAll(shiftsToSave)));
        log.info("Group clock-out completed: {} successful, {} failed", successful.size(), failed.size());

        ClockResponseDTO response = ClockResponseDTO.builder()
                .totalProcessed(request.getEmployeeIds().size())
                .successCount(successful.size())
                .failureCount(failed.size())
                .operationTime(LocalDateTime.now())
                .operationType(ClockOperationType.CLOCK_OUT.name())
                .successful(successful)
                .failed(failed)
                .build();

        if (response.isCompletelySuccessful()) {
            log.info("Clock-out operation COMPLETELY SUCCESSFUL: All {} employees clocked out",
                    response.getSuccessCount());
        } else if (response.isMixedResult()) {
            log.warn("Clock-out operation had PARTIAL FAILURES: {} succeeded, {} failed",
                    response.getSuccessCount(), response.getFailureCount());
        } else {
            log.error("Clock-out operation COMPLETELY FAILED: 0 succeeded, {} failed",
                    response.getFailureCount());
        }

        return response;
    }

    @Override
    public boolean canEmployeeClockIn(Long employeeId) {
        long activeShifts = shiftRepository.countActiveShiftsByEmployeeId(ShiftStatus.ACTIVE, employeeId);
        return activeShifts == 0;
    }

    @Override
    public boolean canEmployeeClockOut(Long employeeId) {
        long activeShifts = shiftRepository.countActiveShiftsByEmployeeId(ShiftStatus.ACTIVE, employeeId);
        return activeShifts > 0;
    }

    @Override
    public Page<ShiftResponseDTO> getAllShifts(Pageable pageable) {
        return shiftRepository.findAll(pageable).map(shiftMapper::toDTO);

        //TODO SHiftValidator will be implemented and check the currentcompanyId for security reasons
    }


    @Override
    public Page<ShiftResponseDTO> getShiftsByEmployeeId(Long employeeId, Pageable pageable) {
        return shiftRepository.findByEmployeeId(employeeId, pageable).map(shiftMapper::toDTO);
    }


    @Override
    public Page<ShiftResponseDTO> getShiftsByJobTitle(JobTitle jobTitle, Pageable pageable) {
        return shiftRepository.findByJobTitle(jobTitle, pageable).map(shiftMapper::toDTO);
    }

    @Override
    public Page<ShiftResponseDTO> getShiftsByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        LocalDateTime startDateTime = toStartOfDay(startDate);
        LocalDateTime endDateTime = toEndOfDay(endDate);
        Page<Shift> shifts = shiftRepository.findByDateRange(startDateTime, endDateTime, pageable);
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
    public Page<ShiftResponseDTO> getShiftByStatusAndEmployeeId(Long employeeId, ShiftStatus status, Pageable pageable) {
        return shiftRepository.findByStatusAndEmployeeId(
                status, employeeId, pageable).map(shiftMapper::toDTO);
    }

    @Override
    public Page<ShiftResponseDTO> getShiftsByStatus(ShiftStatus status,Pageable pageable) {
        return shiftRepository.findAllByStatus(status, pageable).map(shiftMapper::toDTO);
    }

    @Override
    public Page<ShiftResponseDTO> getShiftsFromDate(LocalDate startDate, Pageable pageable) {
        LocalDateTime startDateTime = toStartOfDay(startDate);
        return shiftRepository.findByDateFrom(startDateTime, pageable).map(shiftMapper::toDTO);
    }

    @Override
    public Page<ShiftResponseDTO> getTodaysShifts(Pageable pageable) {
        return getShiftsFromDate(LocalDate.now(), pageable);
    }

    @Override
    public Page<ShiftResponseDTO> getThisWeekShifts(Pageable pageable) {
        return getShiftsFromDate(LocalDate.now().with(DayOfWeek.MONDAY), pageable);
    }

    @Override
    public Page<ShiftResponseDTO> getThisMonthShifts(Pageable pageable) {
        return getShiftsFromDate(LocalDate.now().withDayOfMonth(1), pageable);
    }



    @Override
    public ShiftSummaryDTO getShiftSummary(Long employeeId, LocalDate startDate, LocalDate endDate) {
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

    @Override
    public Shift getActiveShift(Long employeeId) {
        return shiftRepository.findActiveShiftByEmployeeId(employeeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(ClockErrorCode.NO_ACTIVE_SHIFT.getDefaultMessage() + " for employee ID: " + employeeId));
    }

    @Override
    public Map<Long, List<ShiftResponseDTO>> getAllShiftsByDateRange(
             LocalDate startDate, LocalDate endDate, Long companyId) {
        // Single batch query
        List<Shift> shifts = shiftRepository.findAllByCompanyIdAndDateRange(
                startDate, endDate, companyId);

        // Group by empID and convert to DTOs
        return shifts.stream()
                .collect(Collectors.groupingBy(
                        Shift::getEmployeeId,
                        Collectors.mapping(shiftMapper::toDTO, Collectors.toList())
                ));
    }

    /**
     * Builds a failure result for group clock operations
     */
    private ClockFailureResponse buildClockFailure(
            EmployeeJobInfoDTO empJobInfo, String errorMessage, ClockErrorCode errorCode) {
        return ClockFailureResponse.builder()
                .employeeId(empJobInfo.getEmployeeId())
                .employeeJobId(empJobInfo.getEmployeeJobId())
                .username(empJobInfo.getUsername())
                .fullName(empJobInfo.getFullName())
                .jobTitle(empJobInfo.getJobTitle())
                .errorMessage(errorMessage)
                .errorCode(errorCode.name())
                .build();
    }

    /**
     * Builds a failure result for group clock-out operations
     */
    private ClockFailureResponse buildClockOutFailure(
            Long employeeId, String employeeName, String errorMessage, ClockErrorCode errorCode) {
        return ClockFailureResponse.builder()
                .employeeId(employeeId)
                .fullName(employeeName)
                .errorMessage(errorMessage)
                .errorCode(errorCode.name())
                .build();
    }

    private void validateShiftRequest(ShiftRequestDTO request) {
        if (request.getEmployeeJobId() == null) {
            throw new InvalidOperationException("Employee job ID is required");
        }

        if (request.getClockIn() != null && request.getClockOut() != null) {
            validateClockOutTime(request.getClockIn(), request.getClockOut());
        }

        if (request.getNotes() != null && request.getNotes().length() > MAX_NOTES_LENGTH) {
            throw new InvalidOperationException("Notes exceed maximum length of " + MAX_NOTES_LENGTH + " characters");
        }
    }

    private void validateShiftUpdatePermissions(Shift shift) {
        if (shift.getStatus() == ShiftStatus.COMPLETED) {
            // Only allow note updates for completed shifts
            log.warn("Attempting to update completed shift {}", shift.getId());
        }
    }

    private void validateShiftDeletionPermissions(Shift shift) {
        if (shift.getStatus() == ShiftStatus.ACTIVE) {
            throw new InvalidOperationException("Cannot delete active shift. Please clock out first.");
        }
    }





    private void validateClockInRequest(ClockInRequestDTO request) {
        validateListNotEmpty(request.getEmployeeJobIds(), EMPTY_EMPLOYEE_JOB_IDS_MSG);
        validateOperationSize(request.getEmployeeJobIds().size());
        validateTimeNotInFuture(request.getClockInTime(), CLOCK_IN_FUTURE_TIME_MSG);
    }

    private void validateClockOutRequest(ClockOutRequestDTO request) {
        validateListNotEmpty(request.getEmployeeIds(), EMPTY_EMPLOYEE_IDS_MSG);
        validateOperationSize(request.getEmployeeIds().size());
        validateTimeNotInFuture(request.getClockOutTime(), CLOCK_OUT_FUTURE_TIME_MSG);
    }

    // Helper methods
    private void validateListNotEmpty(List<?> list, String errorMessage) {
        if (list == null || list.isEmpty()) {
            throw new InvalidOperationException(errorMessage);
        }
    }

    private void validateOperationSize(int size) {
        if (size > MAX_GROUP_OPERATION_SIZE) {
            throw new InvalidOperationException(OPERATION_SIZE_EXCEEDED_MSG);
        }
    }

    private void validateTimeNotInFuture(LocalDateTime time, String errorMessage) {
        if (time != null && time.isAfter(LocalDateTime.now().plusMinutes(5))) {
            throw new InvalidOperationException(errorMessage);
        }
    }

    private void validateEmployeeJobActive(EmployeeJob employeeJob) {
        if (employeeJob.getEmployee().getStatus() != com.timetrak.enums.EmployeeStatus.ACTIVE) {
            throw new InvalidOperationException("Employee is not active");
        }
    }

    private void validateClockOutTime(LocalDateTime clockIn, LocalDateTime clockOut) {
        if (clockOut.isBefore(clockIn)) {
            throw new InvalidOperationException("Clock out time cannot be before clock in time");
        }

        Duration shiftDuration = Duration.between(clockIn, clockOut);
        if (shiftDuration.toHours() > MAX_SHIFT_DURATION_HOURS) {
            throw new InvalidOperationException("Shift duration cannot exceed " + MAX_SHIFT_DURATION_HOURS + " hours");
        }
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

    /**
     * Sanitizes notes input to prevent XSS and other security issues
     */
    private String sanitizeNotes(String notes) {
        if (notes == null) return null;

        return notes.trim()
                   .replaceAll("<script[^>]*>.*?</script>", "")
                   .replaceAll("<[^>]+>", "");
    }

    /**
     * Truncates notes to prevent database overflow
     */
    private String truncateNotes(String notes) {
        if (notes == null) return null;
        return notes.length() > MAX_NOTES_LENGTH ?
               notes.substring(0, MAX_NOTES_LENGTH - 3) + "..." : notes;
    }

    /**
     * Safely retrieves employee name with fallback for error scenarios
     */
    private String getEmployeeNameSafely(Long employeeId) {
        try {
            return employeeService.getEmployeeNameById(employeeId);
        } catch (Exception e) {
            log.warn("Failed to retrieve employee name for ID: {} - {}", employeeId, e.getMessage());
            return "Unknown Employee (ID: " + employeeId + ")";
        }
    }

}
