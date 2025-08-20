package com.timetrak.service.shift;

import com.timetrak.dto.request.ClockInRequestDTO;
import com.timetrak.dto.request.ClockOutRequestDTO;
import com.timetrak.dto.response.ClockFailureResponse;
import com.timetrak.dto.response.ClockResponseDTO;
import com.timetrak.dto.response.EmployeeJobInfoDTO;
import com.timetrak.dto.response.ShiftResponseDTO;
import com.timetrak.entity.EmployeeJob;
import com.timetrak.entity.Shift;
import com.timetrak.enums.ClockErrorCode;
import com.timetrak.enums.ClockOperationType;
import com.timetrak.enums.ShiftStatus;
import com.timetrak.mapper.ShiftMapper;
import com.timetrak.repository.ShiftRepository;
import com.timetrak.service.EmployeeJobService;
import com.timetrak.service.employee.EmployeeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClockServiceImpl implements ClockService {

    // Clock-in validation messages

    private final ShiftRepository shiftRepository;
    private final ShiftMapper shiftMapper;
    private final EmployeeJobService employeeJobService;
    private final ShiftService shiftService;
    private final ClockValidator validator;
    private final EmployeeService employeeService;

    @Override
    @Transactional
    public ClockResponseDTO clockIn(ClockInRequestDTO request) {
        validator.validateClockInRequest(request);

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
                validator.validateEmployeeJobActive(employeeJob);

                Shift shift = Shift.builder()
                        .clockIn(request.getClockInTime() != null ? request.getClockInTime() : LocalDateTime.now())
                        .employeeJob(employeeJob)
                        .employee(employeeJob.getEmployee())
                        .companyId(empJob.getCompanyId())
                        .status(ShiftStatus.ACTIVE)
                        .notes(validator.sanitizeNotes(request.getNotes()))
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
        validator.validateClockOutRequest(request);

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


                Shift shift = shiftService.getActiveShift(employeeId);
                LocalDateTime clockOutTime = request.getClockOutTime() != null ? request.getClockOutTime() : LocalDateTime.now();

                validator.validateClockOutTime(shift.getClockIn(), clockOutTime);

                shift.setClockOut(clockOutTime);

                String newNotes = validator.sanitizeNotes(request.getNotes());
                if (newNotes != null && !newNotes.trim().isEmpty()) {
                    String existingNotes = shift.getNotes() != null ? shift.getNotes() : "";
                    String combinedNotes = existingNotes.isEmpty() ? newNotes : existingNotes + "\n" + newNotes;
                    shift.setNotes(validator.truncateNotes(combinedNotes));
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
        log.debug("Employee {} has {} active shifts - canClockIn: {}", employeeId, activeShifts, activeShifts == 0);
        return activeShifts == 0;
    }

    @Override
    public boolean canEmployeeClockOut(Long employeeId) {
        long activeShifts = shiftRepository.countActiveShiftsByEmployeeId(ShiftStatus.ACTIVE, employeeId);
        log.debug("Employee {} has {} active shifts - canClockOut: {}", employeeId, activeShifts, activeShifts > 0);
        return activeShifts > 0;
    }

    private String getEmployeeNameSafely(Long employeeId) {
        try {
            return employeeService.getEmployeeNameById(employeeId);
        } catch (Exception e) {
            log.warn("Failed to retrieve employee name for ID: {} - {}", employeeId, e.getMessage());
            return "Unknown Employee (ID: " + employeeId + ")";
        }
    }


    private ClockFailureResponse buildClockOutFailure(
            Long employeeId, String employeeName, String errorMessage, ClockErrorCode errorCode) {
        return ClockFailureResponse.builder()
                .employeeId(employeeId)
                .fullName(employeeName)
                .errorMessage(errorMessage)
                .errorCode(errorCode.name())
                .build();
    }

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

}
