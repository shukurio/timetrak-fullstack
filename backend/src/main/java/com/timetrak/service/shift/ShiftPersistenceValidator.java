package com.timetrak.service.shift;

import com.timetrak.dto.request.ShiftRequestDTO;
import com.timetrak.entity.Shift;
import com.timetrak.enums.ShiftStatus;
import com.timetrak.exception.InvalidOperationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

import static com.timetrak.constant.ShiftConstants.MAX_SHIFT_DURATION_HOURS;

@Component
@Slf4j
public class ShiftPersistenceValidator {

    void validateShiftRequest(ShiftRequestDTO request) {
        if (request.getEmployeeJobId() == null) {
            throw new InvalidOperationException("Employee job ID is required");
        }

        if (request.getClockIn() != null && request.getClockOut() != null) {
            validateClockOutTime(request.getClockIn(), request.getClockOut());
        }

    }

    void validateShiftUpdatePermissions(Shift shift) {
        if (shift.getStatus() == ShiftStatus.COMPLETED) {
            // Completed shift update
            log.warn("Attempting to update completed shift {}", shift.getId());
        }
    }

    void validateShiftDeletionPermissions(Shift shift) {
        if (shift.getStatus() == ShiftStatus.ACTIVE) {
            throw new InvalidOperationException("Cannot delete active shift. Please clock out first.");
        }
    }

    private void validateClockOutTime(LocalDateTime clockIn, LocalDateTime clockOut) {
        if (clockOut.isBefore(clockIn)) {
            throw new InvalidOperationException("Clock out time cannot be before clock in time");
        }

        Duration shiftDuration = Duration.between(clockIn, clockOut);
        if (shiftDuration.toHours() > MAX_SHIFT_DURATION_HOURS) {
            throw new InvalidOperationException("Shift duration cannot exceed " + MAX_SHIFT_DURATION_HOURS + " hours");
        }
    }
}
