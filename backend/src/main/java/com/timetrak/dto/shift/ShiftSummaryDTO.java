package com.timetrak.dto.shift;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

// Create ShiftSummaryDTO instead of Map<String, Object>
@Data
@Builder
public class ShiftSummaryDTO {
    private Long employeeId;
    private String employeeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private int totalShifts;
    private int activeShifts;
    private int completedShifts;
    private Double totalHours;
    private Double totalEarnings;
    private Double averageShiftLength;
}