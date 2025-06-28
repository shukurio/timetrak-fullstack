package com.timetrak.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClockOutRequestDTO {
    /**
     * List of Employee IDs to clock out (we use employee IDs since we need to find their active shifts)
     */
    @NotEmpty(message = "Employee IDs list cannot be empty")
    private List<Long> employeeIds;
    
    /**
     * Clock-out time for all employees (defaults to current time if null)
     */
    private LocalDateTime clockOutTime;
    
    /**
     * Optional notes to add to all shifts being clocked out
     */
    private String notes;
}
