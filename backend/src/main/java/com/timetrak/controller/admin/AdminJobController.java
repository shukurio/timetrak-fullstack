package com.timetrak.controller.admin;

import com.timetrak.dto.job.JobRequestDTO;
import com.timetrak.dto.job.JobResponseDTO;
import com.timetrak.dto.job.JobUpdateDTO;
import com.timetrak.service.auth.AuthContextService;
import com.timetrak.service.job.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/jobs")
@Validated
public class AdminJobController {
    private final AuthContextService authContextService;
    private final JobService jobService;

    @PostMapping("/create")
    public ResponseEntity<JobResponseDTO> createJob(@Valid @RequestBody JobRequestDTO request) {
        JobResponseDTO job = jobService.createJob(request, currentCompanyId());
        return ResponseEntity.status(HttpStatus.CREATED).body(job);
    }

    @PutMapping("/update/{jobId}")
    public ResponseEntity<JobResponseDTO> updateJob(
            @PathVariable Long jobId,
            @Valid @RequestBody JobUpdateDTO dto) {
        JobResponseDTO updated = jobService.updateJob(jobId, dto, currentCompanyId());
        log.debug("Updated successfully: {}", updated.getJobTitle());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/delete/{jobId}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long jobId) {
        jobService.deleteJob(jobId, currentCompanyId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<JobResponseDTO>> getAllJobs() {
        List<JobResponseDTO> jobs = jobService.getAllJobs(currentCompanyId());
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<JobResponseDTO>> getJobsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "jobTitle") String sortBy) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<JobResponseDTO> jobs = jobService.getAllJobsPaged(currentCompanyId(), pageable);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/search")
    public ResponseEntity<List<JobResponseDTO>> searchJobs(
            @RequestParam String query) {
        List<JobResponseDTO> jobs = jobService.searchJobs(query, currentCompanyId());
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<JobResponseDTO> getJob(@PathVariable Long jobId) {
        JobResponseDTO response = jobService.getById(jobId, currentCompanyId());
        return ResponseEntity.ok(response);
    }

    private Long currentCompanyId() {
        return authContextService.getCurrentCompanyId();
    }
}
