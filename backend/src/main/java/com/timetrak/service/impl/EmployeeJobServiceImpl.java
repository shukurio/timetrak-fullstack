package com.timetrak.service.impl;

import com.timetrak.dto.response.EmployeeJobInfoDTO;
import com.timetrak.dto.job.EmployeeJobResponseDTO;
import com.timetrak.entity.EmployeeJob;
import com.timetrak.exception.ResourceNotFoundException;
import com.timetrak.repository.EmployeeJobRepository;
import com.timetrak.service.job.EmployeeJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeJobServiceImpl implements EmployeeJobService {
    private final EmployeeJobRepository employeeJobRepository;

    @Override
    public List<EmployeeJobResponseDTO> getEmployeeJobs(Long employeeId) {
        List<EmployeeJob> employeeJobs = employeeJobRepository.findByEmployeeId(employeeId);
        return mapToDTO(employeeJobs);

    }

    @Override
    public List<EmployeeJobResponseDTO> getEmployeeJobs(String username) {
        List<EmployeeJob> employeeJobs = employeeJobRepository.findByEmployeeUsername(username);
        return mapToDTO(employeeJobs);
    }

    @Override
    public EmployeeJob getEmpJobById(Long id) {
        return employeeJobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeJob not found with id: " + id));
    }

    @Override
    public List<EmployeeJobInfoDTO> getEmpJobsInfoByIds(List<Long> ids) {
        return employeeJobRepository.findByIdsWithEmployeeInfo(ids)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeJob not found with ids: " + ids));
    }

    public List<EmployeeJobResponseDTO> mapToDTO(List<EmployeeJob> employeeJobs) {
        List<EmployeeJobResponseDTO> dtoList = new ArrayList<>();
        for (EmployeeJob employeeJob : employeeJobs) {
            EmployeeJobResponseDTO employeeJobResponseDTO =
                    EmployeeJobResponseDTO.builder()
                            .employeeJobId(employeeJob.getId())
                            .jobTitle(employeeJob.getJob().getJobTitle())
                            .hourlyWage(employeeJob.getJob().getHourlyWage())
                            .build();
            dtoList.add(employeeJobResponseDTO);
        }
        return dtoList;
}
}
