package com.timetrak.service.employeeJob;

import com.timetrak.dto.employeeJob.EmployeeJobRequestDTO;
import com.timetrak.dto.response.EmployeeJobInfoDTO;
import com.timetrak.dto.job.EmployeeJobResponseDTO;
import com.timetrak.entity.EmployeeJob;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface EmployeeJobService {

    EmployeeJobResponseDTO createEmployeeJob(EmployeeJobRequestDTO request);
    List<EmployeeJobResponseDTO> getEmployeeJobs(Long employeeId);

    //For internal use
    EmployeeJob getEmpJobById(Long id);
    List<EmployeeJobInfoDTO> getEmpJobsInfoByIds(List<Long> ids);
}
