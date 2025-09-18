package com.timetrak.controller.user;


import com.timetrak.dto.response.ShiftResponseDTO;
import com.timetrak.dto.response.ShiftSummaryDTO;
import com.timetrak.entity.Shift;
import com.timetrak.enums.ShiftStatus;
import com.timetrak.mapper.ShiftMapper;
import com.timetrak.misc.PageableHelper;
import com.timetrak.service.auth.AuthContextService;
import com.timetrak.service.shift.ShiftService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employee/shifts")
@Validated
@Slf4j
public class ShiftController {
    private final ShiftService shiftService;
    private final ShiftMapper shiftMapper;
    private final AuthContextService contextService;
    private final AuthContextService authContextService;


    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Authentication working!");
    }
    

    // =============== QUERY OPERATIONS ===============

    /**
     * Get shifts by employee ID
     */
    @GetMapping()
    public ResponseEntity<Page<ShiftResponseDTO>> getShiftsByEmployee(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "clockIn") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Pageable pageable = PageableHelper.buildPageable(page, size, sortBy, sortDir);
        


        Page<ShiftResponseDTO> response = shiftService.getShiftsByEmployeeId(currentEmployeeId(),
                currentCompanyId(),
                pageable);
        log.debug("Get shifts by employee {} - page: {}, size: {}", currentEmployeeId(), page, size);


        return ResponseEntity.ok(response);
    }

    /**
     * Get shifts by job title
     */
    @GetMapping("/job-title/{jobTitle}")
    public ResponseEntity<Page<ShiftResponseDTO>> getShiftsByJobTitle(
            @PathVariable String jobTitle,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "clockIn") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Pageable pageable = PageableHelper.buildPageable(page, size, sortBy, sortDir);

        log.debug("Get shifts by job title {} - page: {}, size: {}", jobTitle, page, size);
        
        Page<ShiftResponseDTO> response = shiftService.getShiftsByJobTitle(jobTitle, currentCompanyId(), pageable);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get shifts by status and periodNumber
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
        
        Page<ShiftResponseDTO> response =
                shiftService.getShiftsByDateRange(contextService.getCurrentCompanyId(),
                        startDate,
                        endDate,
                        pageable);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/period/{periodNumber}")
    public ResponseEntity<Page<ShiftResponseDTO>> getShiftsByEmployeeIdAndPeriod(@PathVariable Integer periodNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "clockIn") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir){

        Pageable pageable = PageableHelper.buildPageable(page, size, sortBy, sortDir);


        Page<ShiftResponseDTO> shifts = shiftService.getShiftsByEmployeeIdAndPeriodNumber(periodNumber,
                currentEmployeeId(),
                currentCompanyId(),
                pageable);

        return ResponseEntity.ok(shifts);
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

        Pageable pageable = PageableHelper.buildPageable(page, size, sortBy, sortDir);
        
        log.debug("Get shifts by employee {} and date range {} to {} - page: {}, size: {}", 
                employeeId, startDate, endDate, page, size);
        
        Page<ShiftResponseDTO> response = shiftService.getShiftsByEmployeeIdAndDateRange(
                employeeId, startDate, endDate, pageable);
        
        return ResponseEntity.ok(response);
    }



    // =============== ANALYTICS & REPORTING ===============

    /**
     * Get shift summary for an employee within a date range
     */
    @GetMapping("summary")
    public ResponseEntity<ShiftSummaryDTO> getShiftSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.debug("Get shift summary for employee {} from {} to {}", currentEmployeeId(), startDate, endDate);
        
        ShiftSummaryDTO response = shiftService.getShiftSummaryForEmployee(currentEmployeeId(), startDate, endDate);
        
        return ResponseEntity.ok(response);
    }

    // =============== VALIDATION OPERATIONS ===============


    /**
     * Get active shift for an employee
     */
    @GetMapping("/active")
    public ResponseEntity<ShiftResponseDTO> getActiveShift(){

        log.debug("Get active shift for employee {}", currentEmployeeId());

        Shift shift = shiftService.getActiveShiftSelf(currentEmployeeId());

        if (shift == null) {
            return ResponseEntity.ok(null); // or ResponseEntity.ok().build()
        }
        ShiftResponseDTO response = shiftMapper.toDTO(shift);

        return ResponseEntity.ok(response);
    }

    private Long currentEmployeeId() {
        return authContextService.getCurrentEmployeeId();
    }
    private Long currentCompanyId(){
        return authContextService.getCurrentCompanyId();
    }

}
