package com.timetrak.enums;

public enum PayFrequency {
    WEEKLY("Weekly", 52),
    BIWEEKLY("Bi-weekly", 26),
    MONTHLY("Monthly", 12);

    private final String description;
    private final int periodsPerYear;

    PayFrequency(String description, int periodsPerYear) {
        this.description = description;
        this.periodsPerYear = periodsPerYear;
    }

    public String getDescription() { return description; }
    public int getPeriodsPerYear() { return periodsPerYear; }
}
