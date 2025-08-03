package com.timetrak.dto.payment;


import com.timetrak.enums.PayFrequency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentPeriod {

    // =============== CORE PERIOD INFO ===============
    private LocalDate startDate;
    private LocalDate endDate;
    private PayFrequency frequency;
    private Integer periodNumber;

    // =============== DISPLAY INFO ===============
    private String description;        // "BIWEEKLY 2: Jan 15 - Jan 28, 2024"
    private String shortDescription;   // "Jan 15 - Jan 28"
    private String formattedPeriod;   // "2024-01-15 to 2024-01-28"

    // =============== METADATA ===============
    private boolean isCurrent;        // Is this the current pay period?
    private boolean isPast;           // Is this period in the past?
    private boolean isFuture;         // Is this period in the future?
    private int totalDays;            // Number of days in period

    // =============== CONSTRUCTORS ===============

    public PaymentPeriod(LocalDate startDate, LocalDate endDate, PayFrequency frequency, int periodNumber) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.frequency = frequency;
        this.periodNumber = periodNumber;

        // Auto-calculate derived fields
        this.description = generateDescription();
        this.shortDescription = generateShortDescription();
        this.formattedPeriod = generateFormattedPeriod();
        this.totalDays = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
        this.isCurrent = isCurrentPeriod();
        this.isPast = endDate.isBefore(LocalDate.now());
        this.isFuture = startDate.isAfter(LocalDate.now());
    }

    // =============== HELPER METHODS ===============

    private String generateDescription() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d");
        return String.format("%s %d: %s - %s, %d",
                frequency.toString(),
                periodNumber,
                startDate.format(formatter),
                endDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                startDate.getYear());
    }

    private String generateShortDescription() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d");

        if (startDate.getYear() == endDate.getYear()) {
            if (startDate.getMonth() == endDate.getMonth()) {
                // Same month: "Jan 15 - 28"
                return String.format("%s %d - %d, %s",
                        startDate.format(DateTimeFormatter.ofPattern("MMM")),
                        startDate.getDayOfMonth(),
                        endDate.getDayOfMonth(),
                        endDate.getYear());
            } else {
                // Different months, same year: "Jan 30 - Feb 5"
                return String.format("%s - %s, %s",
                        startDate.format(formatter),
                        endDate.format(formatter),
                        endDate.getYear());
            }
        } else {
            // Different years: "Dec 30, 2023 - Jan 5, 2024"
            return String.format("%s - %s",
                    startDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                    endDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")));
        }
    }

    private String generateFormattedPeriod() {
        return startDate + " to " + endDate;
    }

    private boolean isCurrentPeriod() {
        LocalDate today = LocalDate.now();
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    // =============== BUSINESS METHODS ===============

    public boolean containsDate(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    public PaymentPeriod getNextPeriod() {
        LocalDate nextStart = switch (frequency) {
            case WEEKLY -> startDate.plusWeeks(1);
            case BIWEEKLY -> startDate.plusWeeks(2);
            case MONTHLY -> startDate.plusMonths(1);
        };

        LocalDate nextEnd = switch (frequency) {
            case WEEKLY -> nextStart.plusDays(6);
            case BIWEEKLY -> nextStart.plusDays(13);
            case MONTHLY -> nextStart.plusMonths(1).minusDays(1);
        };

        return new PaymentPeriod(nextStart, nextEnd, frequency, periodNumber + 1);
    }

    public PaymentPeriod getPreviousPeriod() {
        LocalDate prevStart = switch (frequency) {
            case WEEKLY -> startDate.minusWeeks(1);
            case BIWEEKLY -> startDate.minusWeeks(2);
            case MONTHLY -> startDate.minusMonths(1);
        };

        LocalDate prevEnd = switch (frequency) {
            case WEEKLY -> prevStart.plusDays(6);
            case BIWEEKLY -> prevStart.plusDays(13);
            case MONTHLY -> prevStart.plusMonths(1).minusDays(1);
        };

        return new PaymentPeriod(prevStart, prevEnd, frequency, periodNumber - 1);
    }

    public String getDisplayName() {
        if (isCurrent) {
            return "Current Period (" + shortDescription + ")";
        } else if (isPast) {
            return shortDescription + " (Past)";
        } else {
            return shortDescription + " (Future)";
        }
    }

    // =============== VALIDATION ===============

    public boolean isValid() {
        return startDate != null &&
                endDate != null &&
                !startDate.isAfter(endDate) &&
                frequency != null &&
                periodNumber > 0;
    }
}