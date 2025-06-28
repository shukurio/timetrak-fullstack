package com.timetrak.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ClockInRequestDTO {
    /**
     * List of EmployeeJob IDs to clock in (not employee IDs)
     * This allows clocking in employees for specific jobs
     */
    @NotEmpty(message = "EmployeeJob IDs list cannot be empty")
    private List<Long> employeeJobIds;
    
    /**
     * Clock-in time for all employees (defaults to current time if null)
     */
    private LocalDateTime clockInTime;
    
    /**
     * Optional notes to add to all shifts
     */
    private String notes;
}
