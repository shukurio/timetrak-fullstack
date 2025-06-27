package com.timetrak.service.impl;

import com.timetrak.dto.response.EmployeeJobInfoDTO;
import com.timetrak.entity.EmployeeJob;
import com.timetrak.exception.ResourceNotFoundException;
import com.timetrak.repository.EmployeeJobRepository;
import com.timetrak.service.EmployeeJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeJobServiceImpl implements EmployeeJobService {
    private final EmployeeJobRepository employeeJobRepository;

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
}
