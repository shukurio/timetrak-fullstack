package com.tmtrk.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class TwoWeekSummaryDTO {

    // Basic summary info
    private LocalDate startDate;
    private LocalDate endDate;
    private String periodDescription; // e.g., "Dec 1-14, 2024"

    // Work metrics
    private BigDecimal totalHoursWorked;
    private Integer totalShifts;
    private Integer completedShifts;
    private Integer activeShifts;
    private Integer cancelledShifts;

    // Earnings
    private BigDecimal totalEarnings;
    private BigDecimal averageHourlyRate;
    private BigDecimal highestDailyEarnings;
    private BigDecimal lowestDailyEarnings;

    // Time analysis
    private BigDecimal averageShiftDuration; // in hours
    private BigDecimal longestShiftDuration;
    private BigDecimal shortestShiftDuration;
    private Integer daysWorked;
    private Integer daysOff;

    // Job breakdown
    private Integer uniqueJobsWorked;
    private List<JobSummary> jobBreakdown;

    // Daily breakdown
    private List<DailySummary> dailyBreakdown;

    // Performance indicators
    private BigDecimal attendanceRate; // percentage of scheduled vs completed shifts
    private BigDecimal weekOneHours;
    private BigDecimal weekTwoHours;
    private BigDecimal weekOneEarnings;
    private BigDecimal weekTwoEarnings;

    // Constructors
    public TwoWeekSummaryDTO() {}

    public TwoWeekSummaryDTO(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalHoursWorked = BigDecimal.ZERO;
        this.totalEarnings = BigDecimal.ZERO;
        this.totalShifts = 0;
        this.completedShifts = 0;
        this.activeShifts = 0;
        this.cancelledShifts = 0;
        this.daysWorked = 0;
        this.daysOff = 0;
        this.uniqueJobsWorked = 0;
        this.attendanceRate = BigDecimal.ZERO;
        this.weekOneHours = BigDecimal.ZERO;
        this.weekTwoHours = BigDecimal.ZERO;
        this.weekOneEarnings = BigDecimal.ZERO;
        this.weekTwoEarnings = BigDecimal.ZERO;
    }

    // Getters and Setters
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getPeriodDescription() { return periodDescription; }
    public void setPeriodDescription(String periodDescription) { this.periodDescription = periodDescription; }

    public BigDecimal getTotalHoursWorked() { return totalHoursWorked; }
    public void setTotalHoursWorked(BigDecimal totalHoursWorked) { this.totalHoursWorked = totalHoursWorked; }

    public Integer getTotalShifts() { return totalShifts; }
    public void setTotalShifts(Integer totalShifts) { this.totalShifts = totalShifts; }

    public Integer getCompletedShifts() { return completedShifts; }
    public void setCompletedShifts(Integer completedShifts) { this.completedShifts = completedShifts; }

    public Integer getActiveShifts() { return activeShifts; }
    public void setActiveShifts(Integer activeShifts) { this.activeShifts = activeShifts; }

    public Integer getCancelledShifts() { return cancelledShifts; }
    public void setCancelledShifts(Integer cancelledShifts) { this.cancelledShifts = cancelledShifts; }

    public BigDecimal getTotalEarnings() { return totalEarnings; }
    public void setTotalEarnings(BigDecimal totalEarnings) { this.totalEarnings = totalEarnings; }

    public BigDecimal getAverageHourlyRate() { return averageHourlyRate; }
    public void setAverageHourlyRate(BigDecimal averageHourlyRate) { this.averageHourlyRate = averageHourlyRate; }

    public BigDecimal getHighestDailyEarnings() { return highestDailyEarnings; }
    public void setHighestDailyEarnings(BigDecimal highestDailyEarnings) { this.highestDailyEarnings = highestDailyEarnings; }

    public BigDecimal getLowestDailyEarnings() { return lowestDailyEarnings; }
    public void setLowestDailyEarnings(BigDecimal lowestDailyEarnings) { this.lowestDailyEarnings = lowestDailyEarnings; }

    public BigDecimal getAverageShiftDuration() { return averageShiftDuration; }
    public void setAverageShiftDuration(BigDecimal averageShiftDuration) { this.averageShiftDuration = averageShiftDuration; }

    public BigDecimal getLongestShiftDuration() { return longestShiftDuration; }
    public void setLongestShiftDuration(BigDecimal longestShiftDuration) { this.longestShiftDuration = longestShiftDuration; }

    public BigDecimal getShortestShiftDuration() { return shortestShiftDuration; }
    public void setShortestShiftDuration(BigDecimal shortestShiftDuration) { this.shortestShiftDuration = shortestShiftDuration; }

    public Integer getDaysWorked() { return daysWorked; }
    public void setDaysWorked(Integer daysWorked) { this.daysWorked = daysWorked; }

    public Integer getDaysOff() { return daysOff; }
    public void setDaysOff(Integer daysOff) { this.daysOff = daysOff; }

    public Integer getUniqueJobsWorked() { return uniqueJobsWorked; }
    public void setUniqueJobsWorked(Integer uniqueJobsWorked) { this.uniqueJobsWorked = uniqueJobsWorked; }

    public List<JobSummary> getJobBreakdown() { return jobBreakdown; }
    public void setJobBreakdown(List<JobSummary> jobBreakdown) { this.jobBreakdown = jobBreakdown; }

    public List<DailySummary> getDailyBreakdown() { return dailyBreakdown; }
    public void setDailyBreakdown(List<DailySummary> dailyBreakdown) { this.dailyBreakdown = dailyBreakdown; }

    public BigDecimal getAttendanceRate() { return attendanceRate; }
    public void setAttendanceRate(BigDecimal attendanceRate) { this.attendanceRate = attendanceRate; }

    public BigDecimal getWeekOneHours() { return weekOneHours; }
    public void setWeekOneHours(BigDecimal weekOneHours) { this.weekOneHours = weekOneHours; }

    public BigDecimal getWeekTwoHours() { return weekTwoHours; }
    public void setWeekTwoHours(BigDecimal weekTwoHours) { this.weekTwoHours = weekTwoHours; }

    public BigDecimal getWeekOneEarnings() { return weekOneEarnings; }
    public void setWeekOneEarnings(BigDecimal weekOneEarnings) { this.weekOneEarnings = weekOneEarnings; }

    public BigDecimal getWeekTwoEarnings() { return weekTwoEarnings; }
    public void setWeekTwoEarnings(BigDecimal weekTwoEarnings) { this.weekTwoEarnings = weekTwoEarnings; }

    // Helper methods
    public boolean hasWorkedInPeriod() {
        return totalShifts != null && totalShifts > 0;
    }

    public BigDecimal getWeeklyAverageHours() {
        return totalHoursWorked != null ? totalHoursWorked.divide(BigDecimal.valueOf(2), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;
    }

    public BigDecimal getWeeklyAverageEarnings() {
        return totalEarnings != null ? totalEarnings.divide(BigDecimal.valueOf(2), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;
    }

    public BigDecimal getDailyAverageHours() {
        return daysWorked != null && daysWorked > 0 && totalHoursWorked != null
            ? totalHoursWorked.divide(BigDecimal.valueOf(daysWorked), 2, BigDecimal.ROUND_HALF_UP)
            : BigDecimal.ZERO;
    }

    public BigDecimal getDailyAverageEarnings() {
        return daysWorked != null && daysWorked > 0 && totalEarnings != null
            ? totalEarnings.divide(BigDecimal.valueOf(daysWorked), 2, BigDecimal.ROUND_HALF_UP)
            : BigDecimal.ZERO;
    }

    // Nested classes for breakdown data
    public static class JobSummary {
        private String jobTitle;
        private String departmentName;
        private BigDecimal hoursWorked;
        private BigDecimal earnings;
        private Integer shiftsCount;
        private BigDecimal hourlyRate;
        private BigDecimal percentageOfTotalHours;
        private BigDecimal percentageOfTotalEarnings;

        // Constructors
        public JobSummary() {}

        public JobSummary(String jobTitle, String departmentName, BigDecimal hoursWorked,
                         BigDecimal earnings, Integer shiftsCount, BigDecimal hourlyRate) {
            this.jobTitle = jobTitle;
            this.departmentName = departmentName;
            this.hoursWorked = hoursWorked;
            this.earnings = earnings;
            this.shiftsCount = shiftsCount;
            this.hourlyRate = hourlyRate;
        }

        // Getters and Setters
        public String getJobTitle() { return jobTitle; }
        public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

        public String getDepartmentName() { return departmentName; }
        public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

        public BigDecimal getHoursWorked() { return hoursWorked; }
        public void setHoursWorked(BigDecimal hoursWorked) { this.hoursWorked = hoursWorked; }

        public BigDecimal getEarnings() { return earnings; }
        public void setEarnings(BigDecimal earnings) { this.earnings = earnings; }

        public Integer getShiftsCount() { return shiftsCount; }
        public void setShiftsCount(Integer shiftsCount) { this.shiftsCount = shiftsCount; }

        public BigDecimal getHourlyRate() { return hourlyRate; }
        public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }

        public BigDecimal getPercentageOfTotalHours() { return percentageOfTotalHours; }
        public void setPercentageOfTotalHours(BigDecimal percentageOfTotalHours) { this.percentageOfTotalHours = percentageOfTotalHours; }

        public BigDecimal getPercentageOfTotalEarnings() { return percentageOfTotalEarnings; }
        public void setPercentageOfTotalEarnings(BigDecimal percentageOfTotalEarnings) { this.percentageOfTotalEarnings = percentageOfTotalEarnings; }
    }

    public static class DailySummary {
        private LocalDate date;
        private String dayName; // Monday, Tuesday, etc.
        private BigDecimal hoursWorked;
        private BigDecimal earnings;
        private Integer shiftsCount;
        private List<String> jobTitles;
        private boolean wasWorkDay;

        // Constructors
        public DailySummary() {}

        public DailySummary(LocalDate date, String dayName, BigDecimal hoursWorked,
                           BigDecimal earnings, Integer shiftsCount, boolean wasWorkDay) {
            this.date = date;
            this.dayName = dayName;
            this.hoursWorked = hoursWorked;
            this.earnings = earnings;
            this.shiftsCount = shiftsCount;
            this.wasWorkDay = wasWorkDay;
        }

        // Getters and Setters
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }

        public String getDayName() { return dayName; }
        public void setDayName(String dayName) { this.dayName = dayName; }

        public BigDecimal getHoursWorked() { return hoursWorked; }
        public void setHoursWorked(BigDecimal hoursWorked) { this.hoursWorked = hoursWorked; }

        public BigDecimal getEarnings() { return earnings; }
        public void setEarnings(BigDecimal earnings) { this.earnings = earnings; }

        public Integer getShiftsCount() { return shiftsCount; }
        public void setShiftsCount(Integer shiftsCount) { this.shiftsCount = shiftsCount; }

        public List<String> getJobTitles() { return jobTitles; }
        public void setJobTitles(List<String> jobTitles) { this.jobTitles = jobTitles; }

        public boolean isWasWorkDay() { return wasWorkDay; }
        public void setWasWorkDay(boolean wasWorkDay) { this.wasWorkDay = wasWorkDay; }
    }
}