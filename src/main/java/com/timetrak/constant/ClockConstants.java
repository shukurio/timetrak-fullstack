package com.timetrak.constant;


public final class ClockConstants {
    private ClockConstants() {
    }

    public static final String ALREADY_CLOCKED_IN = "Employee is already clocked in. Must clock out first.";
    public static final String NOT_CLOCKED_IN = "Employee is not clocked in. Must clock in first.";

    public static final String OUT_OF_RADIUS = "Clock operation failed: not at workplace location";
}
