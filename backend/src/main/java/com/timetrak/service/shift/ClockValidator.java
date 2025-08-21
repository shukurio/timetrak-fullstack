package com.timetrak.service.shift;

import com.timetrak.dto.request.ClockInRequestDTO;
import com.timetrak.dto.request.ClockOutRequestDTO;
import com.timetrak.entity.EmployeeJob;
import com.timetrak.exception.InvalidOperationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static com.timetrak.constant.ShiftConstants.*;

@Component
@Slf4j
public class ClockValidator {


    // Shared messages
    private static final String OPERATION_SIZE_EXCEEDED_MSG =
            "Group operation size exceeds maximum allowed: " + MAX_GROUP_OPERATION_SIZE;

    void validateClockInRequest(ClockInRequestDTO request) {
        validateListNotEmpty(request.getEmployeeJobIds(), EMPTY_EMPLOYEE_JOB_IDS_MSG);
        validateOperationSize(request.getEmployeeJobIds().size());
        validateTimeNotInFuture(request.getClockInTime(), CLOCK_IN_FUTURE_TIME_MSG);
    }

    private void validateListNotEmpty(List<?> list, String errorMessage) {
        if (list == null || list.isEmpty()) {
            throw new InvalidOperationException(errorMessage);
        }
    }

    private void validateOperationSize(int size) {
        if (size > MAX_GROUP_OPERATION_SIZE) {
            throw new InvalidOperationException(OPERATION_SIZE_EXCEEDED_MSG);
        }
    }

    private void validateTimeNotInFuture(LocalDateTime time, String errorMessage) {
        if (time != null && time.isAfter(LocalDateTime.now().plusMinutes(5))) {
            throw new InvalidOperationException(errorMessage);
        }
    }

    void validateEmployeeJobActive(EmployeeJob employeeJob) {
        if (employeeJob.getEmployee().getStatus() != com.timetrak.enums.EmployeeStatus.ACTIVE) {
            throw new InvalidOperationException("Employee is not active");
        }
    }

    void validateClockOutTime(LocalDateTime clockIn, LocalDateTime clockOut) {
        if (clockOut.isBefore(clockIn)) {
            throw new InvalidOperationException("Clock out time cannot be before clock in time");
        }

        Duration shiftDuration = Duration.between(clockIn, clockOut);
        if (shiftDuration.toHours() > MAX_SHIFT_DURATION_HOURS) {
            throw new InvalidOperationException("Shift duration cannot exceed " + MAX_SHIFT_DURATION_HOURS + " hours");
        }
    }

    void validateClockOutRequest(ClockOutRequestDTO request) {
        validateListNotEmpty(request.getEmployeeIds(), EMPTY_EMPLOYEE_IDS_MSG);
        validateOperationSize(request.getEmployeeIds().size());
        validateTimeNotInFuture(request.getClockOutTime(), CLOCK_OUT_FUTURE_TIME_MSG);
    }

    String sanitizeNotes(String notes) {
        if (notes == null) return null;

        return notes.trim()
                .replaceAll("<script[^>]*>.*?</script>", "")
                .replaceAll("<[^>]+>", "");
    }

    String truncateNotes(String notes) {
        if (notes == null) return null;
        return notes.length() > MAX_NOTES_LENGTH ?
                notes.substring(0, MAX_NOTES_LENGTH - 3) + "..." : notes;
    }
}
