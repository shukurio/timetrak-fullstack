package com.timetrak.service.employeeJob;

import com.timetrak.dto.employeeJob.EmployeeJobRequestDTO;
import com.timetrak.dto.employeeJob.EmployeeJobUpdateDTO;
import com.timetrak.entity.Employee;
import com.timetrak.entity.EmployeeJob;
import com.timetrak.entity.Job;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeJobValidationService {
    
    private final EmployeeJobQueryService queryService;

    /**
     * Validates bulk assignment request
     */
    public void validateBulkAssignmentRequest(EmployeeJobRequestDTO request) {
        if (request.getEmployeeIds() == null || request.getEmployeeIds().isEmpty()) {
            throw new IllegalArgumentException("Employee IDs list cannot be empty");
        }
        
        if (request.getEmployeeIds().size() > 50) {
            throw new IllegalArgumentException("Cannot assign more than 50 employees at once");
        }
        
        if (request.getJobId() == null) {
            throw new IllegalArgumentException("Job ID is required");
        }
        
        validateHourlyWage(request.getHourlyWage());
    }

    /**
     * Validates bulk removal request
     */
    public void validateBulkRemovalRequest(List<Long> employeeIds, Long jobId) {
        if (employeeIds == null || employeeIds.isEmpty()) {
            throw new IllegalArgumentException("Employee IDs list cannot be empty");
        }
        
        if (employeeIds.size() > 50) {
            throw new IllegalArgumentException("Cannot remove more than 50 employees at once");
        }
        
        if (jobId == null) {
            throw new IllegalArgumentException("Job ID is required");
        }
    }

    /**
     * Validates individual assignment request
     */
    public void validateIndividualAssignment(Long employeeId, Long jobId, BigDecimal hourlyWage, Long companyId) {
        if (employeeId == null) {
            throw new IllegalArgumentException("Employee ID is required");
        }
        
        if (jobId == null) {
            throw new IllegalArgumentException("Job ID is required");
        }
        
        validateHourlyWage(hourlyWage);
        
        if (queryService.existsAssignment(employeeId, jobId, companyId)) {
            throw new IllegalArgumentException("Employee already assigned to this job");
        }
    }

    /**
     * Validates update request
     */
    public void validateUpdateRequest(EmployeeJobUpdateDTO request, Long employeeJobId) {
        if (employeeJobId == null) {
            throw new IllegalArgumentException("EmployeeJob ID is required");
        }
        
        if (request.getHourlyWage() != null) {
            validateHourlyWage(request.getHourlyWage());
        }
    }

    /**
     * Validates employee is active and belongs to company
     */
    public void validateEmployee(Employee employee, Long companyId) {
        if (employee == null) {
            throw new IllegalArgumentException("Employee cannot be null");
        }
        
        if (employee.isDeleted()) {
            throw new IllegalArgumentException("Employee is deleted");
        }
        
        if (!employee.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Employee does not belong to the specified company");
        }
        
        // Note: Employee status is already validated by getActiveById() method
    }

    /**
     * Validates job exists and belongs to company
     */
    public void validateJob(Job job, Long companyId) {
        if (job == null) {
            throw new IllegalArgumentException("Job cannot be null");
        }
        
        if (job.isDeleted()) {
            throw new IllegalArgumentException("Job is deleted");
        }
        
        if (!job.getCompanyId().equals(companyId)) {
            throw new IllegalArgumentException("Job does not belong to the specified company");
        }
    }

    /**
     * Validates EmployeeJob exists and belongs to company
     */
    public void validateEmployeeJob(EmployeeJob employeeJob, Long companyId) {
        if (employeeJob == null) {
            throw new IllegalArgumentException("EmployeeJob cannot be null");
        }
        
        if (employeeJob.isDeleted()) {
            throw new IllegalArgumentException("EmployeeJob assignment is deleted");
        }
        
        if (!employeeJob.getEmployee().getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("EmployeeJob does not belong to the specified company");
        }
    }

    /**
     * Validates hourly wage is positive and within acceptable range
     */
    public void validateHourlyWage(BigDecimal hourlyWage) {
        if (hourlyWage != null) {
            if (hourlyWage.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Hourly wage must be positive");
            }
            
            if (hourlyWage.compareTo(new BigDecimal("999.99")) > 0) {
                throw new IllegalArgumentException("Hourly wage cannot exceed $999.99");
            }
            
            if (hourlyWage.scale() > 2) {
                throw new IllegalArgumentException("Hourly wage can have at most 2 decimal places");
            }
        }
    }
}
