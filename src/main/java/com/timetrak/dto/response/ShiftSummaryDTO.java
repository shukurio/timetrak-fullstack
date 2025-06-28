package com.timetrak.dto.response;

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
    private double totalHours;
    private double totalEarnings;
    private double averageShiftLength;
}