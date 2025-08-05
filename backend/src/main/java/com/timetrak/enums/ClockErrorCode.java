package com.timetrak.enums;

public enum ClockErrorCode {
    ALREADY_CLOCKED_IN("Employee already has active shift"),
    NO_ACTIVE_SHIFT("No active shift to clock out from"),
    CLOCK_IN_ERROR("Error occurred during clock-in operation"),
    CLOCK_OUT_ERROR("Error occurred during clock-out operation"),
    EMPLOYEE_NOT_FOUND("Employee not found"),
    INVALID_OPERATION("Invalid clock operation"),
    SHIFT_NOT_FOUND("Shift not found"),
    EMPLOYEE_JOB_NOT_FOUND("Employee job assignment not found");

    private final String defaultMessage;

    ClockErrorCode(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
} 