package com.timetrak.service.job;

import com.timetrak.dto.job.JobRequestDTO;
import com.timetrak.dto.job.JobResponseDTO;
import com.timetrak.dto.job.JobUpdateDTO;
import com.timetrak.entity.Job;
import com.timetrak.exception.DuplicateResourceException;
import com.timetrak.exception.ResourceNotFoundException;
import com.timetrak.mapper.JobMapper;
import com.timetrak.repository.JobRepository;
import com.timetrak.service.CompanyService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class JobServiceImpl implements JobService {
    private final JobMapper jobMapper;
    private final CompanyService companyService;
    private final JobRepository jobRepo;

    @Override
    @Transactional(readOnly = true)
    public JobResponseDTO getById(Long jobId,Long companyId) {
        Job job = getByIdEntity(jobId,companyId);
        return jobMapper.toDTO(job);
    }

    @Transactional(readOnly = true)
    @Override
    public Job getByIdEntity(Long jobId,Long companyId) {
        return jobRepo.findByIdAndCompanyIdAndDeletedAtIsNull(jobId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Job with ID " + jobId + " not found for company " + companyId
                ));
    }


    @Override
    @Transactional
    public JobResponseDTO createJob(JobRequestDTO request, Long companyId) {
        validateJobTitle(request.getJobTitle(), companyId);

        Job job =jobMapper.toEntity(request);
        job.setCompany(companyService.getCompanyById(companyId));
        Job savedJob = jobRepo.save(job);

        return jobMapper.toDTO(savedJob);
    }

    @Override
    @Transactional
    public void deleteJob(Long jobId, Long companyId) {
        Job job = getByIdEntity(jobId, companyId);
        job.markAsDeleted();
        jobRepo.save(job);
    }


    @Override
    @Transactional
    public JobResponseDTO updateJob(Long jobId,JobUpdateDTO request, Long companyId) {

        Job job = getByIdEntity(jobId,companyId);

        if (request.getJobTitle() != null) {
            String upperTitle = request.getJobTitle().toUpperCase();

            if (!upperTitle.equalsIgnoreCase(job.getJobTitle())) {
                validateJobTitle(upperTitle, companyId);
            }
        }

        Job updated =jobMapper.updateJobFromDto(request, job);

        return jobMapper.toDTO(jobRepo.save(updated));
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobResponseDTO> getAllJobs(Long companyId) {
        List<Job> jobs = jobRepo.findByCompanyIdAndDeletedAtIsNull(companyId);
        return jobMapper.toDTOList(jobs);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobResponseDTO> getAllJobsPaged(Long companyId, Pageable pageable) {
        Page<Job> jobs = jobRepo.findByCompanyIdAndDeletedAtIsNull(companyId,pageable);
        return jobs.map(jobMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobResponseDTO> searchJobs(String query, Long companyId) {
        if (query == null || query.trim().isEmpty()) {
            return getAllJobs(companyId);
        }

        List<Job> jobs = jobRepo.searchByJobTitle(query.trim(), companyId);
        return jobMapper.toDTOList(jobs);
    }

    private void validateJobTitle(String jobTitle, Long companyId) {
        if (jobTitle == null) return;

        if (jobRepo.existsByJobTitleAndCompanyIdAndDeletedAtIsNull(jobTitle, companyId)) {
            throw new DuplicateResourceException("Job title '" + jobTitle + "' already exists");
        }
    }
}
