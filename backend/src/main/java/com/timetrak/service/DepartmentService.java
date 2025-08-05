package com.timetrak.service;

import com.timetrak.dto.response.DepartmentResponseDTO;
import com.timetrak.dto.request.DepartmentRequestDTO;
import com.timetrak.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DepartmentService {
    Page<DepartmentResponseDTO> getAllByCompanyId(Long companyId, Pageable pageable);

    Department getDepartmentById(Long id, Long companyId);

    DepartmentResponseDTO getDepartmentDTOById(Long id, Long companyId);

    DepartmentResponseDTO addDepartment(DepartmentRequestDTO department, Long companyId);

    void deleteDepartment(Long id, Long companyId);

    DepartmentResponseDTO updateDepartment(Long id,Long companyId, DepartmentRequestDTO updatedRequest);

    DepartmentResponseDTO getDepartmentByCode(String code, Long companyId);

    boolean validateDepartmentBelongToCompany(Long departmentId, Long companyId);
}