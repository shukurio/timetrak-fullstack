package com.timetrak.service.employeeJob;

import com.timetrak.dto.employeeJob.EmployeeJobResponseDTO;
import com.timetrak.entity.EmployeeJob;

import java.util.List;

public interface EmployeeJobQueryService {

    // Query operations (READ ONLY)
    List<EmployeeJobResponseDTO> getEmployeeJobs(Long employeeId, Long companyId);
    EmployeeJobResponseDTO getEmployeeJobKiosk(Long employeeId);//Kiosk Only


    List<EmployeeJobResponseDTO> getJobAssignments(Long jobId, Long companyId);
    List<EmployeeJobResponseDTO> getDepartmentAssignments(Long departmentId, Long companyId);
    List<EmployeeJobResponseDTO> getAllAssignments(Long companyId);
    List<EmployeeJobResponseDTO> getEmployeeJobsByUsername(String username, Long companyId);
    
    // Internal use (for clock operations and other services)
    EmployeeJob getEmployeeJobById(Long employeeJobId, Long companyId);
    List<EmployeeJobResponseDTO> getEmployeeJobsInfoByIds(List<Long> employeeJobIds);
    
    // Existence checks
    boolean existsAssignment(Long employeeId, Long jobId, Long companyId);


}
