package com.timetrak.controller;

import com.timetrak.dto.request.ClockInRequestDTO;
import com.timetrak.dto.request.ClockOutRequestDTO;
import com.timetrak.dto.request.ShiftRequestDTO;
import com.timetrak.dto.response.ClockResponseDTO;
import com.timetrak.dto.response.ShiftResponseDTO;
import com.timetrak.dto.response.ShiftSummaryDTO;
import com.timetrak.entity.Shift;
import com.timetrak.enums.JobTitle;
import com.timetrak.enums.ShiftStatus;
import com.timetrak.mapper.ShiftMapper;
import com.timetrak.service.ShiftService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * REST controller for managing employee shifts and time tracking operations.
 * Provides endpoints for clock operations, shift management, and reporting.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shifts")
@Validated
@Slf4j
public class ShiftController {
    private final ShiftService shiftService;
    private final ShiftMapper shiftMapper;
    
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Authentication working!");
    }
    

    /**
     * Clock in multiple employees simultaneously (Admin operation)
     */
    @PostMapping("/clock-in")
    public ResponseEntity<ClockResponseDTO> clockIn(@Valid @NotNull @RequestBody ClockInRequestDTO request) {
        log.info("Group clock-in request received for {} employees", request.getEmployeeJobIds().size());

        ClockResponseDTO response = shiftService.clockIn(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Clock out multiple employees simultaneously (Admin operation)
     */
    @PostMapping("/clock-out")
    public ResponseEntity<ClockResponseDTO> clockOut(@Valid @NotNull @RequestBody ClockOutRequestDTO request) {
        log.info("Group clock-out request received for {} employees", request.getEmployeeIds().size());

        ClockResponseDTO response = shiftService.clockOut(request);
        
        log.info("Group clock-out completed: {} successful, {} failed", 
                response.getSuccessCount(), response.getFailureCount());
        
        return ResponseEntity.ok(response);
    }

    // =============== CRUD OPERATIONS ===============

    /**
     * Create a new shift manually (Admin operation)
     */
    @PostMapping
    public ResponseEntity<ShiftResponseDTO> createShift(@Valid @NotNull @RequestBody ShiftRequestDTO request) {
        log.info("Create shift request received for employee job ID: {}", request.getEmployeeJobId());
        
        ShiftResponseDTO response = shiftService.createShift(request);
        
        log.info("Shift created successfully with ID: {}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing shift (Admin operation)
     */
    @PutMapping("/{shiftId}")
    public ResponseEntity<ShiftResponseDTO> updateShift(
            @PathVariable @Positive(message = "Shift ID must be positive") Long shiftId,
            @Valid @NotNull @RequestBody ShiftRequestDTO request) {
        
        log.info("Update shift request received for shift ID: {}", shiftId);
        
        ShiftResponseDTO response = shiftService.updateShift(shiftId, request);
        
        log.info("Shift {} updated successfully", shiftId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a specific shift by ID
     */
    @GetMapping("/{shiftId}")
    public ResponseEntity<ShiftResponseDTO> getShiftById(
            @PathVariable @Positive(message = "Shift ID must be positive") Long shiftId) {
        
        log.debug("Get shift request for ID: {}", shiftId);
        
        Shift shift = shiftService.getShiftById(shiftId);
        return ResponseEntity.ok(shiftMapper.toDTO(shift));
    }

    /**
     * Soft delete a shift (Admin operation)
     */
    @DeleteMapping("/{shiftId}")
    public ResponseEntity<Void> deleteShift(
            @PathVariable @Positive(message = "Shift ID must be positive") Long shiftId) {
        
        log.info("Delete shift request for ID: {}", shiftId);
        
        shiftService.deleteShift(shiftId);
        
        log.info("Shift {} deleted successfully", shiftId);
        return ResponseEntity.noContent().build();
    }

    // =============== QUERY OPERATIONS ===============

    /**
     * Get all shifts with pagination and sorting
     */
    @GetMapping
    public ResponseEntity<Page<ShiftResponseDTO>> getAllShifts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "clockIn") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        log.debug("Get all shifts request - page: {}, size: {}, sort: {} {}", page, size, sortBy, sortDir);
        
        Page<ShiftResponseDTO> response = shiftService.getAllShifts(pageable);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get shifts by employee ID
     */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<Page<ShiftResponseDTO>> getShiftsByEmployee(
            @PathVariable @Positive(message = "Employee ID must be positive") Long employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "clockIn") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        log.debug("Get shifts by employee {} - page: {}, size: {}", employeeId, page, size);
        
        Page<ShiftResponseDTO> response = shiftService.getShiftsByEmployeeId(employeeId, pageable);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get shifts by job title
     */
    @GetMapping("/job-title/{jobTitle}")
    public ResponseEntity<Page<ShiftResponseDTO>> getShiftsByJobTitle(
            @PathVariable JobTitle jobTitle,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "clockIn") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        log.debug("Get shifts by job title {} - page: {}, size: {}", jobTitle, page, size);
        
        Page<ShiftResponseDTO> response = shiftService.getShiftsByJobTitle(jobTitle, pageable);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get shifts by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<ShiftResponseDTO>> getShiftsByStatus(
            @PathVariable ShiftStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "clockIn") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        log.debug("Get shifts by status {} - page: {}, size: {}", status, page, size);
        
        Page<ShiftResponseDTO> response = shiftService.getShiftsByStatus(status, pageable);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get shifts by employee and status
     */
    @GetMapping("/employee/{employeeId}/status/{status}")
    public ResponseEntity<Page<ShiftResponseDTO>> getShiftsByEmployeeAndStatus(
            @PathVariable @Positive(message = "Employee ID must be positive") Long employeeId,
            @PathVariable ShiftStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "clockIn") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        log.debug("Get shifts by employee {} and status {} - page: {}, size: {}", employeeId, status, page, size);
        
        Page<ShiftResponseDTO> response = shiftService.getShiftByStatusAndEmployeeId(employeeId, status, pageable);
        
        return ResponseEntity.ok(response);
    }

    // =============== DATE RANGE QUERIES ===============

    /**
     * Get shifts within a date range
     */
    @GetMapping("/date-range")
    public ResponseEntity<Page<ShiftResponseDTO>> getShiftsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "clockIn") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        log.debug("Get shifts by date range {} to {} - page: {}, size: {}", startDate, endDate, page, size);
        
        Page<ShiftResponseDTO> response = shiftService.getShiftsByDateRange(startDate, endDate, pageable);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get shifts by employee within a date range
     */
    @GetMapping("/employee/{employeeId}/date-range")
    public ResponseEntity<Page<ShiftResponseDTO>> getShiftsByEmployeeAndDateRange(
            @PathVariable @Positive(message = "Employee ID must be positive") Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "clockIn") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        log.debug("Get shifts by employee {} and date range {} to {} - page: {}, size: {}", 
                employeeId, startDate, endDate, page, size);
        
        Page<ShiftResponseDTO> response = shiftService.getShiftsByEmployeeIdAndDateRange(
                employeeId, startDate, endDate, pageable);
        
        return ResponseEntity.ok(response);
    }

    // =============== QUICK REPORTS ===============

    /**
     * Get today's shifts
     */
    @GetMapping("/today")
    public ResponseEntity<Page<ShiftResponseDTO>> getTodaysShifts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "clockIn") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        log.debug("Get today's shifts - page: {}, size: {}", page, size);
        
        Page<ShiftResponseDTO> response = shiftService.getTodaysShifts(pageable);
        
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
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        log.debug("Get this week's shifts - page: {}, size: {}", page, size);
        
        Page<ShiftResponseDTO> response = shiftService.getThisWeekShifts(pageable);
        
        return ResponseEntity.ok(response);
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
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        log.debug("Get this month's shifts - page: {}, size: {}", page, size);
        
        Page<ShiftResponseDTO> response = shiftService.getThisMonthShifts(pageable);
        
        return ResponseEntity.ok(response);
    }

    // =============== ANALYTICS & REPORTING ===============

    /**
     * Get shift summary for an employee within a date range
     */
    @GetMapping("/employee/{employeeId}/summary")
    public ResponseEntity<ShiftSummaryDTO> getShiftSummary(
            @PathVariable @Positive(message = "Employee ID must be positive") Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.debug("Get shift summary for employee {} from {} to {}", employeeId, startDate, endDate);
        
        ShiftSummaryDTO response = shiftService.getShiftSummary(employeeId, startDate, endDate);
        
        return ResponseEntity.ok(response);
    }

    // =============== VALIDATION OPERATIONS ===============

    @GetMapping("/employee/{employeeId}/can-clock-in")
    public ResponseEntity<Boolean> canEmployeeClockIn(
            @PathVariable @Positive(message = "Employee ID must be positive") Long employeeId) {
        log.debug("Check if employee {} can clock in", employeeId);
        return ResponseEntity.ok(shiftService.canEmployeeClockIn(employeeId));
    }

    @GetMapping("/employee/{employeeId}/can-clock-out")
    public ResponseEntity<Boolean> canEmployeeClockOut(
            @PathVariable @Positive(message = "Employee ID must be positive") Long employeeId) {
        log.debug("Check if employee {} can clock out", employeeId);
        return ResponseEntity.ok(shiftService.canEmployeeClockOut(employeeId));
    }

    /**
     * Get active shift for an employee
     */
    @GetMapping("/employee/{employeeId}/active")
    public ResponseEntity<ShiftResponseDTO> getActiveShift(
            @PathVariable @Positive(message = "Employee ID must be positive") Long employeeId) {
        
        log.debug("Get active shift for employee {}", employeeId);
        
        Shift shift = shiftService.getActiveShift(employeeId);
        ShiftResponseDTO response = shiftMapper.toDTO(shift);
        
        return ResponseEntity.ok(response);
    }

}
