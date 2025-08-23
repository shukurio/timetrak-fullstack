package com.timetrak.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClockFailureResponse {

    /**
     * Employee ID (for internal processing)
     */
    private Long employeeId;

    /**
     * EmployeeJob ID that failed
     */
    private Long employeeJobId;


    /**
     * Employee full name (for better UX)
     */
    private String fullName;

    /**
     * Job title they were trying to clock in/out for
     */
    private String jobTitle;

    /**
     * Error message explaining why it failed
     */
    private String errorMessage;

    /**
     * Error code for programmatic handling
     */
    private String errorCode;

}