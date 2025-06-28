package com.timetrak.dto.response;

import com.timetrak.enums.JobTitle;
import com.timetrak.enums.ShiftStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftResponseDTO {

    private Long id;
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    private String notes;
    private ShiftStatus status;

    private Long employeeId;
    private Long employeeJobId;
    private String username;
    private String fullName;

    private JobTitle jobTitle;
    private java.math.BigDecimal hourlyWage;

    public boolean isActive() {
        return status == ShiftStatus.ACTIVE;
    }


    /**
     * Calculates total hours worked for completed shifts.
     */
    public Double getTotalHours() {
        if (clockIn != null && clockOut != null) {
            return java.time.Duration.between(clockIn, clockOut).toMinutes() / 60.0;
        }
        return null;
    }
}
