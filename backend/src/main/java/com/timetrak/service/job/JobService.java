package com.timetrak.service.job;

import com.timetrak.dto.job.JobRequestDTO;
import com.timetrak.dto.job.JobResponseDTO;
import com.timetrak.dto.job.JobUpdateDTO;
import com.timetrak.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface JobService {
    JobResponseDTO getById(Long jobId,Long companyId);
    Job getByIdEntity(Long jobId, Long companyId);

    JobResponseDTO createJob(JobRequestDTO request,Long companyId);
    void deleteJob(Long jobId, Long companyId);
    JobResponseDTO updateJob(Long jobId,JobUpdateDTO request, Long companyId);
    List<JobResponseDTO> getAllJobs(Long companyId);
    Page<JobResponseDTO> getAllJobsPaged(Long companyId, Pageable pageable);
    List<JobResponseDTO> searchJobs(String query, Long companyId);

}
