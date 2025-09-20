package com.timetrak.service.shift;

import com.timetrak.dto.clock.AdminClockRequestDTO;
import com.timetrak.dto.clock.EmployeeClockRequestDTO;
import com.timetrak.dto.employeeJob.EmployeeJobResponseDTO;
import com.timetrak.dto.response.ClockFailureResponse;
import com.timetrak.dto.response.ClockResponseDTO;
import com.timetrak.dto.response.EmployeeResponseDTO;
import com.timetrak.dto.response.ShiftResponseDTO;
import com.timetrak.entity.EmployeeJob;
import com.timetrak.entity.Shift;
import com.timetrak.enums.ClockErrorCode;
import com.timetrak.enums.ClockAction;
import com.timetrak.enums.ShiftStatus;
import com.timetrak.exception.InvalidOperationException;
import com.timetrak.mapper.ShiftMapper;
import com.timetrak.repository.ShiftRepository;
import com.timetrak.service.employeeJob.EmployeeJobQueryService;
import com.timetrak.service.employee.EmployeeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.timetrak.constant.ClockConstants.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClockServiceImpl implements ClockService {

    private final ShiftRepository shiftRepository;
    private final ShiftMapper shiftMapper;
    private final EmployeeJobQueryService employeeJobQueryService;
    private final ShiftService shiftService;
    private final ClockValidator validator;
    private final EmployeeService employeeService;
    private final LocationService locationService;

    @Override
    @Transactional
    public ClockResponseDTO adminClockIn(AdminClockRequestDTO request) {
        validator.validateClockInRequest(request);

        List<EmployeeJobResponseDTO> employeeJobs = employeeJobQueryService.getEmployeeJobsInfoByIds(request.getIds());
        List<ClockFailureResponse> failed = new ArrayList<>();
        List<Shift> shiftsToSave = new ArrayList<>();

        for (EmployeeJobResponseDTO empJob : employeeJobs) {
            try {
                if (!canEmployeeClockIn(empJob.getEmployeeId())) {
                    failed.add(buildClockFailure(empJob, ClockErrorCode.ALREADY_CLOCKED_IN.getDefaultMessage(), ClockErrorCode.ALREADY_CLOCKED_IN));
                    continue;
                }

                EmployeeJob employeeJob = employeeJobQueryService.getEmployeeJobById(empJob.getEmployeeJobId(), empJob.getCompanyId());
                validator.validateEmployeeJobActive(employeeJob);

                Shift shift = Shift.builder()
                        .clockIn(request.getTime() != null ? request.getTime() : LocalDateTime.now())
                        .employeeJob(employeeJob)
                        .employee(employeeJob.getEmployee())
                        .companyId(empJob.getCompanyId())
                        .status(ShiftStatus.ACTIVE)
                        .build();

                shiftsToSave.add(shift);

            } catch (Exception e) {
                log.error("Failed to clock in employee: {} - {}", empJob.getEmployeeName(), e.getMessage(), e);
                failed.add(buildClockFailure(empJob, e.getMessage(), ClockErrorCode.CLOCK_IN_ERROR));
            }
        }


        List<ShiftResponseDTO> successful = new ArrayList<>(shiftMapper.toDTOList(shiftRepository.saveAll(shiftsToSave)));

        ClockResponseDTO response = ClockResponseDTO.builder()
                .totalProcessed(employeeJobs.size())
                .successCount(successful.size())
                .failureCount(failed.size())
                .operationTime(LocalDateTime.now())
                .operationType(ClockAction.CLOCK_IN.name())
                .successful(successful)
                .failed(failed)
                .build();
        if (response.isMixedResult()) {
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
    public ClockResponseDTO adminClockOut(AdminClockRequestDTO request) {
        validator.validateClockOutRequest(request);

        List<ClockFailureResponse> failed = new ArrayList<>();
        List<Shift> shiftsToSave = new ArrayList<>();

        for (Long employeeId : request.getIds()) {
            try {
                if (!canEmployeeClockOut(employeeId)) {
                    String employeeName = getEmployeeNameSafely(employeeId);
                    failed.add(buildClockOutFailure(employeeId, employeeName, ClockErrorCode.NO_ACTIVE_SHIFT.getDefaultMessage(), ClockErrorCode.NO_ACTIVE_SHIFT));
                    continue;
                }

                Shift shift = shiftService.getActiveShiftSelf(employeeId);
                LocalDateTime clockOutTime = request.getTime() != null ? request.getTime() : LocalDateTime.now();

                validator.validateClockOutTime(shift.getClockIn(), clockOutTime);
                shift.setClockOut(clockOutTime);
                shift.setStatus(ShiftStatus.COMPLETED);
                shiftsToSave.add(shift);
            } catch (Exception e) {
                log.error("Failed to clock out employee ID: {} - {}", employeeId, e.getMessage(), e);
                String employeeName = getEmployeeNameSafely(employeeId);
                failed.add(buildClockOutFailure(employeeId, employeeName, e.getMessage(), ClockErrorCode.CLOCK_OUT_ERROR));
            }
        }

        List<ShiftResponseDTO> successful = new ArrayList<>(shiftMapper.toDTOList(shiftRepository.saveAll(shiftsToSave)));

        ClockResponseDTO response = ClockResponseDTO.builder()
                .totalProcessed(request.getIds().size())
                .successCount(successful.size())
                .failureCount(failed.size())
                .operationTime(LocalDateTime.now())
                .operationType(ClockAction.CLOCK_OUT.name())
                .successful(successful)
                .failed(failed)
                .build();
        if (response.isMixedResult()) {
            log.warn("Clock-out operation had PARTIAL FAILURES: {} succeeded, {} failed",
                    response.getSuccessCount(), response.getFailureCount());
        } else {
            log.error("Clock-out operation COMPLETELY FAILED: 0 succeeded, {} failed",
                    response.getFailureCount());
        }

        return response;
    }

    @Override
    public ShiftResponseDTO employeeClockIn(EmployeeClockRequestDTO request, Long companyId) {
        try {
            validateInRadius(request.getLatitude(), request.getLongitude(),companyId);

            EmployeeJob employeeJob = employeeJobQueryService.getEmployeeJobById(request.getId(), companyId);

            if (!canEmployeeClockIn(employeeJob.getEmployee().getId())) {
                throw new InvalidOperationException(ALREADY_CLOCKED_IN);
            }

            validator.validateEmployeeJobActive(employeeJob);

            Shift shift = Shift.builder()
                    .clockIn(LocalDateTime.now()) // System always uses current time
                    .employeeJob(employeeJob)
                    .employee(employeeJob.getEmployee())
                    .companyId(companyId)
                    .status(ShiftStatus.ACTIVE)
                    .build();

            Shift savedShift = shiftRepository.save(shift);

            return shiftMapper.toDTO(savedShift);

        } catch (InvalidOperationException e) {
            log.warn("Employee clock-in failed for employeeJob ID {}: {}", request.getId(), e.getMessage());
            throw e; // Re-throw for proper error handling
        }
    }

    @Override
    public ShiftResponseDTO employeeClockOut(EmployeeClockRequestDTO request, Long companyId) {
        try {
            validateInRadius(request.getLatitude(), request.getLongitude(),companyId);

            Long employeeId = request.getId();

            if (!canEmployeeClockOut(employeeId)) {
                throw new InvalidOperationException(NOT_CLOCKED_IN);
            }

            Shift activeShift = shiftService.getActiveShiftSelf(employeeId);

            LocalDateTime clockOutTime = LocalDateTime.now();

            validator.validateClockOutTime(activeShift.getClockIn(), clockOutTime);

            activeShift.setClockOut(clockOutTime);
            activeShift.setStatus(ShiftStatus.COMPLETED);
            Shift savedShift = shiftRepository.save(activeShift);

            return shiftMapper.toDTO(savedShift);

        } catch (InvalidOperationException e) {
            log.warn("Employee clock-out failed for employee ID {}: {}", request.getId(), e.getMessage());
            throw e; // Re-throw for proper error handling
        } catch (Exception e) {
            log.error("Unexpected error during employee clock-out for employee ID {}: {}", request.getId(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public ShiftResponseDTO kioskClockIn(EmployeeClockRequestDTO request) {
        try {

            EmployeeJobResponseDTO empJob = employeeJobQueryService.getEmployeeJobKiosk(request.getId());
            validateInRadius(request.getLatitude(), request.getLongitude(),empJob.getCompanyId());

            if (!canEmployeeClockIn(empJob.getEmployeeId())) {
                throw new InvalidOperationException(ALREADY_CLOCKED_IN);
            }

            EmployeeJob employeeJob = employeeJobQueryService.getEmployeeJobById(empJob.getEmployeeJobId(), empJob.getCompanyId());

            validator.validateEmployeeJobActive(employeeJob);

            Shift shift = Shift.builder()
                    .clockIn(LocalDateTime.now()) // Kiosk always uses current time
                    .employeeJob(employeeJob)
                    .employee(employeeJob.getEmployee())
                    .companyId(empJob.getCompanyId())
                    .status(ShiftStatus.ACTIVE)
                    .build();

            Shift savedShift = shiftRepository.save(shift);

            return shiftMapper.toDTO(savedShift);

        } catch (InvalidOperationException e) {
            log.warn("Kiosk clock-in failed for employeeJob ID {}: {}", request.getId(), e.getMessage());
            throw e; // Re-throw for proper error handling
        } catch (Exception e) {
            log.error("Unexpected error during kiosk clock-in for employeeJob ID {}: {}", request.getId(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public ShiftResponseDTO kioskClockOut(EmployeeClockRequestDTO request) {
        try {
            EmployeeResponseDTO employee =  employeeService.getEmployeeDTOById(request.getId());

            validateInRadius(request.getLatitude(), request.getLongitude(),employee.getCompanyId());

            if (!canEmployeeClockOut(employee.getId())) {
                throw new InvalidOperationException(NOT_CLOCKED_IN);
            }

            Shift activeShift = shiftService.getActiveShiftSelf(employee.getId());

            LocalDateTime clockOutTime = LocalDateTime.now();

            validator.validateClockOutTime(activeShift.getClockIn(), clockOutTime);

            activeShift.setClockOut(clockOutTime);

            activeShift.setStatus(ShiftStatus.COMPLETED);

            Shift savedShift = shiftRepository.save(activeShift);

            return shiftMapper.toDTO(savedShift);

        } catch (InvalidOperationException e) {
            log.warn("Kiosk clock-out failed for employee ID {}: {}", request.getId(), e.getMessage());
            throw e; // Re-throw for proper error handling
        } catch (Exception e) {
            log.error("Unexpected error during kiosk clock-out for employee ID {}: {}", request.getId(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public ClockAction determineAction(Long employeeId) {
        boolean canClockIn = canEmployeeClockIn(employeeId);
        boolean canClockOut = canEmployeeClockOut(employeeId);
        ClockAction action;

        if (canClockOut) {
            action = ClockAction.CLOCK_OUT;
        } else if (canClockIn) {
            action = ClockAction.CLOCK_IN;
        } else {
            log.warn("Cannot determine action for employee {}",
                    employeeId);
            action = ClockAction.UNAVAILABLE;
        }
        return action;
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
            EmployeeJobResponseDTO empJobResponse, String errorMessage, ClockErrorCode errorCode) {
        return ClockFailureResponse.builder()
                .employeeId(empJobResponse.getEmployeeId())
                .employeeJobId(empJobResponse.getEmployeeJobId())
                .jobTitle(empJobResponse.getJobTitle())
                .errorMessage(errorMessage)
                .errorCode(errorCode.name())
                .build();
    }

    private void validateInRadius(Double lat, Double lng, Long companyId){
        if(!locationService.isWithinAllowedRadius(lat,lng,companyId)) {
            throw new IllegalArgumentException(OUT_OF_RADIUS);
        }
    }


}
