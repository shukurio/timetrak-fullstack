package com.timetrak.dto.response;

import com.timetrak.enums.JobTitle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for efficient batch queries containing employee and job information.
 * Used for group clock operations to avoid N+1 query problems.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeJobInfoDTO {
    
    /**
     * EmployeeJob ID (primary key)
     */
    private Long employeeJobId;
    
    /**
     * Employee ID
     */
    private Long employeeId;
    
    /**
     * Employee username
     */
    private String username;
    
    /**
     * Employee first name
     */
    private String firstName;
    
    /**
     * Employee last name
     */
    private String lastName;
    
    /**
     * Job title enum
     */
    private JobTitle jobTitle;
    
    /**
     * Hourly wage for this job
     */
    private java.math.BigDecimal hourlyWage;
    
    /**
     * Company ID (for validation)
     */
    private Long companyId;
    
    /**
     * Helper method to get full name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
} 