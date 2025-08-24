package com.timetrak.service.employeeJob;

import com.timetrak.dto.employeeJob.EmployeeJobResponseDTO;
import com.timetrak.entity.EmployeeJob;
import com.timetrak.exception.ResourceNotFoundException;
import com.timetrak.mapper.EmployeeJobMapper;
import com.timetrak.repository.EmployeeJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EmployeeJobQueryServiceImpl implements EmployeeJobQueryService {
    
    private final EmployeeJobRepository employeeJobRepository;
    private final EmployeeJobMapper employeeJobMapper;

    @Override
    public List<EmployeeJobResponseDTO> getEmployeeJobs(Long employeeId, Long companyId) {
        log.debug("Retrieving jobs for employee ID: {} in company: {}", employeeId, companyId);
        List<EmployeeJob> employeeJobs = employeeJobRepository.findByEmployeeIdAndCompanyId(employeeId, companyId);
        return employeeJobMapper.toDTOList(employeeJobs);
    }

    @Override
    public EmployeeJobResponseDTO getEmployeeJobKiosk(Long empJobId) {
        
       EmployeeJob response =  employeeJobRepository.findByIdAndDeletedAtIsNull(empJobId)
               .orElseThrow(()-> new ResourceNotFoundException("EmployeeJob not found with id : "+ empJobId ));
       return employeeJobMapper.toDTO(response);

    }

    @Override
    public List<EmployeeJobResponseDTO> getJobAssignments(Long jobId, Long companyId) {
        log.debug("Retrieving assignments for job ID: {} in company: {}", jobId, companyId);
        List<EmployeeJob> assignments = employeeJobRepository.findByJobIdAndCompanyId(jobId, companyId);
        return employeeJobMapper.toDTOList(assignments);
    }

    @Override
    public List<EmployeeJobResponseDTO> getDepartmentAssignments(Long departmentId, Long companyId) {
        log.debug("Retrieving assignments for department ID: {} in company: {}", departmentId, companyId);
        List<EmployeeJob> assignments = employeeJobRepository.findByJobDepartmentIdAndCompanyId(departmentId, companyId);
        return employeeJobMapper.toDTOList(assignments);
    }

    @Override
    public List<EmployeeJobResponseDTO> getAllAssignments(Long companyId) {
        log.debug("Retrieving all assignments for company: {}", companyId);
        List<EmployeeJob> assignments = employeeJobRepository.findByCompanyIdAndDeletedAtIsNull(companyId);
        return employeeJobMapper.toDTOList(assignments);
    }

    @Override
    public List<EmployeeJobResponseDTO> getEmployeeJobsByUsername(String username, Long companyId) {
        log.debug("Retrieving jobs for username: {} in company: {}", username, companyId);
        List<EmployeeJob> employeeJobs = employeeJobRepository.findByEmployeeUsernameAndCompanyId(username, companyId);
        return employeeJobMapper.toDTOList(employeeJobs);
    }

    @Override
    public EmployeeJob getEmployeeJobById(Long employeeJobId, Long companyId) {
        log.debug("Retrieving EmployeeJob by ID: {} in company: {}", employeeJobId, companyId);
        return employeeJobRepository.findByIdAndCompanyId(employeeJobId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeJob not found with id: " + employeeJobId));
    }

    @Override
    public List<EmployeeJobResponseDTO> getEmployeeJobsInfoByIds(List<Long> employeeJobIds) {
        List<EmployeeJob> employeeJobs = employeeJobRepository.findByIdsWithEmployeeInfo(employeeJobIds);
        return employeeJobMapper.toDTOList(employeeJobs);
    }

    @Override
    public boolean existsAssignment(Long employeeId, Long jobId, Long companyId) {
        log.debug("Checking assignment existence for employee: {} and job: {} in company: {}", employeeId, jobId, companyId);
        return employeeJobRepository.existsByEmployeeIdAndJobIdAndCompanyId(employeeId, jobId, companyId);
    }
}
