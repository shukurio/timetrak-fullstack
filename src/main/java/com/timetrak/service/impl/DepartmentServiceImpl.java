package com.timetrak.service.impl;

import com.timetrak.entity.Department;
import com.timetrak.exception.ResourceNotFoundException;
import com.timetrak.repository.DepartmentRepository;
import com.timetrak.service.DepartmentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;


    @Override
    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
    }

//    public DepartmentResponseDTO mapToDTO(Department department) {
//        return DepartmentResponseDTO.builder()
//                .id(department.getId())
//                .name(department.getName())
//                .code(department.getCode())
//                .description(department.getDescription())
//                .isActive(department.getIsActive())
//                .companyId(department.getCompany().getId())
//                .build();
//
//    }
}
