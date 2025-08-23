package com.timetrak.constant;

public final class ShiftConstants {
    public ShiftConstants() {}

    public static final String EMPTY_EMPLOYEE_JOB_IDS_MSG = "Employee job IDs list cannot be empty";
    public static final String CLOCK_IN_FUTURE_TIME_MSG = "Clock in time cannot be in the future";

    // Clock-out validation messages
    public static final String EMPTY_EMPLOYEE_IDS_MSG = "Employee IDs list cannot be empty";
    public static final String CLOCK_OUT_FUTURE_TIME_MSG = "Clock out time cannot be in the future";

    public static final double PRECISION_FACTOR = 100.0;
    public static final int MAX_GROUP_OPERATION_SIZE = 100;
    public static final int MAX_SHIFT_DURATION_HOURS = 24;
    public static final int MAX_NOTES_LENGTH = 1000;
}
