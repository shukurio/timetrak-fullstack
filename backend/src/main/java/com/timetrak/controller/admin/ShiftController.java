package com.timetrak.controller.admin;

import com.timetrak.dto.clock.AdminClockRequestDTO;
import com.timetrak.dto.request.ShiftRequestDTO;
import com.timetrak.dto.response.ClockResponseDTO;
import com.timetrak.dto.response.ShiftResponseDTO;
import com.timetrak.enums.ShiftStatus;
import com.timetrak.misc.PageableHelper;
import com.timetrak.service.auth.AuthContextService;
import com.timetrak.service.clock.ClockService;
import com.timetrak.service.shift.ShiftPersistenceService;
import com.timetrak.service.shift.ShiftService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


/**
 * REST controller for managing employee shifts and time tracking operations.
 * Provides endpoints for clock operations, shift management, and reporting.
 */

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/admin/shifts/")
@PreAuthorize("hasRole('ADMIN')")
public class ShiftController {

    private final ShiftService shiftService;
    private final ClockService clockService;
    private final ShiftPersistenceService persistenceService;
    private final AuthContextService authContextService;

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<Page<ShiftResponseDTO>> getShiftsByDepartment(
            @PathVariable Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "clockIn") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir){

        Pageable pageable = PageableHelper.buildPageable(page, size, sortBy, sortDir);

        Page<ShiftResponseDTO> shifts = shiftService.getShiftsByDepartment(departmentId,currentCompanyId(),pageable);
        return ResponseEntity.ok(shifts);

    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<Page<ShiftResponseDTO>> getShiftsByEmployeeId(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "clockIn") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir){

        Pageable pageable = PageableHelper.buildPageable(page, size, sortBy, sortDir);


        Page<ShiftResponseDTO> shifts = shiftService.getShiftsByEmployeeId(employeeId,currentCompanyId(),pageable);

            log.debug("Get  shifts for employee - page: {}, size: {}", page, size);
            return ResponseEntity.ok(shifts);

    }

    @PutMapping("/{shiftId}")
    public ResponseEntity<ShiftResponseDTO> updateShift(
            @PathVariable @Positive(message = "Shift ID must be positive") Long shiftId,
            @Valid @NotNull @RequestBody ShiftRequestDTO request) {

        log.info("Update shift request received for shift ID: {}", shiftId);

        ShiftResponseDTO response = persistenceService.updateShift(shiftId, request);

        log.info("Shift {} updated successfully", shiftId);
        return ResponseEntity.ok(response);
    }

    /**
     * Clock in multiple employees simultaneously (Admin operation)
     */
    @PostMapping("/clock-in")
    public ResponseEntity<ClockResponseDTO> clockIn(@Valid @NotNull @RequestBody AdminClockRequestDTO request) {
        log.info("Group clock-in request received for {} employees", request.getIds().size());

        ClockResponseDTO response = clockService.adminClockIn(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Clock out multiple employees simultaneously (Admin operation)
     */
    @PostMapping("/clock-out")
    public ResponseEntity<ClockResponseDTO> clockOut(@Valid @NotNull @RequestBody AdminClockRequestDTO request) {
        log.info("Group clock-out request received for {} employees", request.getIds().size());

        ClockResponseDTO response = clockService.adminClockOut(request);

        log.info("Group clock-out completed: {} successful, {} failed",
                response.getSuccessCount(), response.getFailureCount());

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ShiftResponseDTO> createShift(@Valid @NotNull @RequestBody ShiftRequestDTO request) {
        log.info("Create shift request received for employee job ID: {}", request.getEmployeeJobId());

        ShiftResponseDTO response = persistenceService.createShift(request,currentCompanyId());

        log.info("Shift created successfully with ID: {}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }



    /**
     * Get a specific shift by ID
     */
    @GetMapping("/{shiftId}")
    public ResponseEntity<ShiftResponseDTO> getShiftById(
            @PathVariable @Positive(message = "Shift ID must be positive") Long shiftId) {

        log.debug("Get shift request for ID: {}", shiftId);

        ShiftResponseDTO shift = shiftService.getShiftById(shiftId);
        return ResponseEntity.ok(shift);
    }

    /**
     * Soft delete a shift (Admin operation)
     */
    @DeleteMapping("/{shiftId}")
    public ResponseEntity<Void> deleteShift(
            @PathVariable @Positive(message = "Shift ID must be positive") Long shiftId) {

        log.info("Delete shift request for ID: {}", shiftId);

        persistenceService.deleteShift(shiftId);

        log.info("Shift {} deleted successfully", shiftId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get this month's shifts
     */
    @GetMapping("/this-month")
    public ResponseEntity<Page<ShiftResponseDTO>> getThisMonthShifts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "clockIn") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Pageable pageable = PageableHelper.buildPageable(page, size, sortBy, sortDir);


        log.debug("Get this month's shifts - page: {}, size: {}", page, size);

        Page<ShiftResponseDTO> response = shiftService.getThisMonthShifts(currentCompanyId(),pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get this week's shifts
     */
    @GetMapping("/this-week")
    public ResponseEntity<Page<ShiftResponseDTO>> getThisWeekShifts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "clockIn") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Pageable pageable = PageableHelper.buildPageable(page, size, sortBy, sortDir);


        log.debug("Get this week's shifts - page: {}, size: {}", page, size);

        Page<ShiftResponseDTO> response = shiftService.getThisWeekShifts(currentCompanyId(),pageable);

        return ResponseEntity.ok(response);
    }



    /**
     * Get shifts by employee and status
     */
    @GetMapping("period/{periodNumber}/status/{status}")
    public ResponseEntity<Page<ShiftResponseDTO>> getShiftsByStatus(
            @PathVariable @Positive Integer periodNumber,
            @PathVariable ShiftStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "clockIn") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Pageable pageable = PageableHelper.buildPageable(page, size, sortBy, sortDir);


        Page<ShiftResponseDTO> response = shiftService.getShiftsByStatusAndPeriodNumber(periodNumber,
                status,
                currentCompanyId(),
                pageable);
        log.debug("Get shifts by employee {} and status {} - page: {}, size: {}",
                currentEmployeeId(), status, page, size);


        return ResponseEntity.ok(response);
    }

    @GetMapping("/periodNumber/{periodNumber}")
    public ResponseEntity<Page<ShiftResponseDTO>> getShiftsByPeriodNumber(
            @PathVariable Integer periodNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "clockIn") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir){

        Pageable pageable = PageableHelper.buildPageable(page, size, sortBy, sortDir);

        Page<ShiftResponseDTO> shifts = shiftService.getShiftsByPeriodNumber(periodNumber, currentCompanyId(), pageable);
        return ResponseEntity.ok(shifts);
    }

    private Long currentEmployeeId() {
        return authContextService.getCurrentEmployeeId();
    }
    private Long currentCompanyId(){
        return authContextService.getCurrentCompanyId();
    }


}
