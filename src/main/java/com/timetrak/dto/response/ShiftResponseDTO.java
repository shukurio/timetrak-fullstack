package com.timetrak.dto.response;

import com.timetrak.enums.JobTitle;
import com.timetrak.enums.ShiftStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
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
    private BigDecimal hourlyWage;
    private BigDecimal totalHours;
    private BigDecimal shiftEarnings;

    public boolean isActive() {
        return status == ShiftStatus.ACTIVE;
    }



    public BigDecimal getTotalHours() {
        if (clockIn != null && clockOut != null) {
            double duration = Duration.between(clockIn, clockOut).toMinutes() / 60.0;

            return BigDecimal.valueOf(duration);
        }
        return null;
    }

    public BigDecimal getShiftEarnings() {
        if((hourlyWage != null) && getTotalHours() != null && status == ShiftStatus.COMPLETED) {
            BigDecimal earnings = hourlyWage.multiply(getTotalHours());
            return earnings.setScale(2, RoundingMode.HALF_UP);
        }
        return null;
    }
}
