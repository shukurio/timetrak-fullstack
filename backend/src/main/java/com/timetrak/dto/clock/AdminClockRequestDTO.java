package com.timetrak.dto.clock;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AdminClockRequestDTO {
    @NotEmpty(message = "Id list cannot be empty")
    private List<Long> ids; //can be EmployeeJobsIds or EmployeeIds

    private LocalDateTime time; // Can set past/future times
    private String reason;
}