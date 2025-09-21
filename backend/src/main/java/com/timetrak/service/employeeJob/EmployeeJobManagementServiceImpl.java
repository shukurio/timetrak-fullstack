package com.timetrak.service.employeeJob;

import com.timetrak.dto.employeeJob.*;
import com.timetrak.entity.Employee;
import com.timetrak.entity.EmployeeJob;
import com.timetrak.entity.Job;
import com.timetrak.mapper.EmployeeJobMapper;
import com.timetrak.repository.EmployeeJobRepository;
import com.timetrak.service.employee.EmployeeService;
import com.timetrak.service.job.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmployeeJobManagementServiceImpl implements EmployeeJobManagementService {
    
    private final EmployeeJobRepository employeeJobRepository;
    private final EmployeeJobMapper employeeJobMapper;
    private final EmployeeJobQueryService queryService;
    private final EmployeeJobValidationService validationService;
    private final EmployeeService employeeService;
    private final JobService jobService;

    @Override
    public EmployeeJobBulkResponseDTO assignJobToEmployees(EmployeeJobRequestDTO request, Long companyId) {
        log.info("Starting bulk job assignment: {} employees to job {} in company {}", 
                request.getEmployeeIds().size(), request.getJobId(), companyId);
        
        validationService.validateBulkAssignmentRequest(request);
        
        Job job = jobService.getByIdEntity(request.getJobId(), companyId);
        validationService.validateJob(job, companyId);
        
        List<EmployeeJobResponseDTO> successful = new ArrayList<>();
        List<EmployeeJobFailureResponseDTO> failed = new ArrayList<>();
        
        for (Long employeeId : request.getEmployeeIds()) {
            try {
                processIndividualAssignment(employeeId, job, request.getHourlyWage(), companyId, successful, failed);
            } catch (Exception e) {
                String employeeName = getEmployeeNameSafely(employeeId, companyId);
                failed.add(buildFailureResponse(employeeId, employeeName, e.getMessage(), "ASSIGNMENT_ERROR"));
                log.error("Failed to assign employee {} to job {}: {}", employeeId, request.getJobId(), e.getMessage());
            }
        }
        
        log.info("Bulk assignment completed: {} successful, {} failed", successful.size(), failed.size());
        
        return buildBulkResponse(request.getEmployeeIds().size(), successful, failed, "BULK_ASSIGN");
    }

    @Override
    public EmployeeJobBulkResponseDTO removeJobFromEmployees(List<Long> employeeIds, Long jobId, Long companyId) {
        log.info("Starting bulk job removal: {} employees from job {} in company {}", 
                employeeIds.size(), jobId, companyId);
        
        validationService.validateBulkRemovalRequest(employeeIds, jobId);
        
        Job job = jobService.getByIdEntity(jobId, companyId);
        validationService.validateJob(job, companyId);
        
        List<EmployeeJobResponseDTO> successful = new ArrayList<>();
        List<EmployeeJobFailureResponseDTO> failed = new ArrayList<>();
        
        for (Long employeeId : employeeIds) {
            try {
                processIndividualRemoval(employeeId, jobId, companyId, successful, failed);
            } catch (Exception e) {
                String employeeName = getEmployeeNameSafely(employeeId, companyId);
                failed.add(buildFailureResponse(employeeId, employeeName, e.getMessage(), "REMOVAL_ERROR"));
                log.error("Failed to remove employee {} from job {}: {}", employeeId, jobId, e.getMessage());
            }
        }
        
        log.info("Bulk removal completed: {} successful, {} failed", successful.size(), failed.size());
        
        return buildBulkResponse(employeeIds.size(), successful, failed, "BULK_REMOVE");
    }

    @Override
    public EmployeeJobResponseDTO createAssignment(Long employeeId, Long jobId, BigDecimal hourlyWage, Long companyId) {
        log.info("Creating individual assignment: employee {} to job {} in company {}", employeeId, jobId, companyId);
        
        validationService.validateIndividualAssignment(employeeId, jobId, hourlyWage, companyId);
        
        Employee employee = employeeService.getActiveById(employeeId, companyId);
        validationService.validateEmployee(employee, companyId);
        
        Job job = jobService.getByIdEntity(jobId, companyId);
        validationService.validateJob(job, companyId);
        
        EmployeeJob assignment = EmployeeJob.builder()
                .employee(employee)
                .job(job)
                .hourlyWage(hourlyWage)
                .build();
        
        EmployeeJob saved = employeeJobRepository.save(assignment);
        log.info("Successfully created assignment ID: {} for employee {} to job {}", 
                saved.getId(), employee.getUsername(), job.getJobTitle());
        
        return employeeJobMapper.toDTO(saved);
    }

    @Override
    public void deleteAssignment(Long employeeJobId, Long companyId) {
        log.info("Deleting assignment ID: {} in company {}", employeeJobId, companyId);
        
        EmployeeJob assignment = queryService.getEmployeeJobById(employeeJobId, companyId);
        validationService.validateEmployeeJob(assignment, companyId);
        
        assignment.markAsDeleted();
        employeeJobRepository.save(assignment);
        
        log.info("Successfully deleted assignment ID: {} for employee {} from job {}", 
                employeeJobId, assignment.getEmployee().getUsername(), assignment.getJob().getJobTitle());
    }

    @Override
    public EmployeeJobResponseDTO updateAssignment(Long employeeJobId, EmployeeJobUpdateDTO request, Long companyId) {
        log.info("Updating assignment ID: {} in company {}", employeeJobId, companyId);
        
        validationService.validateUpdateRequest(request, employeeJobId);
        
        EmployeeJob assignment = queryService.getEmployeeJobById(employeeJobId, companyId);
        validationService.validateEmployeeJob(assignment, companyId);
        
        employeeJobMapper.updateEmployeeJobFromDto(request, assignment);
        EmployeeJob updated = employeeJobRepository.save(assignment);
        
        log.info("Successfully updated assignment ID: {} with new wage: {}", 
                employeeJobId, updated.getEffectiveHourlyWage());
        
        return employeeJobMapper.toDTO(updated);
    }

    // Helper methods
    private void processIndividualAssignment(Long employeeId, Job job, BigDecimal hourlyWage, 
                                           Long companyId, List<EmployeeJobResponseDTO> successful, 
                                           List<EmployeeJobFailureResponseDTO> failed) {
        Employee employee = employeeService.getActiveById(employeeId, companyId);
        validationService.validateEmployee(employee, companyId);
        
        if (queryService.existsAssignment(employeeId, job.getId(), companyId)) {
            failed.add(buildFailureResponse(employeeId, employee.getFullName(), 
                    "Employee already assigned to this job", "DUPLICATE_ASSIGNMENT"));
            return;
        }
        
        EmployeeJob assignment = EmployeeJob.builder()
                .employee(employee)
                .job(job)
                .hourlyWage(hourlyWage)
                .build();
        
        EmployeeJob saved = employeeJobRepository.save(assignment);
        successful.add(employeeJobMapper.toDTO(saved));
        
        log.debug("Successfully assigned employee {} to job {} with wage {}", 
                employee.getUsername(), job.getJobTitle(), assignment.getEffectiveHourlyWage());
    }

    private void processIndividualRemoval(Long employeeId, Long jobId, Long companyId, 
                                        List<EmployeeJobResponseDTO> successful, 
                                        List<EmployeeJobFailureResponseDTO> failed) {
        Employee employee = employeeService.getActiveById(employeeId, companyId);
        validationService.validateEmployee(employee, companyId);
        
        List<EmployeeJob> assignments = employeeJobRepository.findByEmployeeIdAndCompanyId(employeeId, companyId);
        EmployeeJob assignment = assignments.stream()
                .filter(ej -> ej.getJob().getId().equals(jobId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Employee not assigned to this job"));
        
        assignment.markAsDeleted();
        EmployeeJob saved = employeeJobRepository.save(assignment);
        successful.add(employeeJobMapper.toDTO(saved));
        
        log.debug("Successfully removed employee {} from job {}", 
                employee.getUsername(), assignment.getJob().getJobTitle());
    }

    private EmployeeJobBulkResponseDTO buildBulkResponse(int totalProcessed, 
                                                       List<EmployeeJobResponseDTO> successful, 
                                                       List<EmployeeJobFailureResponseDTO> failed, 
                                                       String operationType) {
        return EmployeeJobBulkResponseDTO.builder()
                .totalProcessed(totalProcessed)
                .successCount(successful.size())
                .failureCount(failed.size())
                .operationType(operationType)
                .operationTime(LocalDateTime.now())
                .successful(successful)
                .failed(failed)
                .build();
    }

    private EmployeeJobFailureResponseDTO buildFailureResponse(Long employeeId, String employeeName, 
                                                             String errorMessage, String errorCode) {
        return EmployeeJobFailureResponseDTO.builder()
                .employeeId(employeeId)
                .employeeName(employeeName)
                .errorMessage(errorMessage)
                .errorCode(errorCode)
                .build();
    }

    private String getEmployeeNameSafely(Long employeeId, Long companyId) {
        try {
            Employee employee = employeeService.getActiveById(employeeId, companyId);
            return employee.getFullName();
        } catch (Exception e) {
            log.warn("Failed to retrieve employee name for ID: {} - {}", employeeId, e.getMessage());
            return "Unknown Employee (ID: " + employeeId + ")";
        }
    }
}
