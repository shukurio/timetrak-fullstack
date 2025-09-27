package com.timetrak.service.department;

import com.timetrak.dto.department.DepartmentInfoDTO;
import com.timetrak.dto.department.DepartmentResponseDTO;
import com.timetrak.dto.department.DepartmentRequestDTO;
import com.timetrak.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DepartmentService {
    Page<DepartmentResponseDTO> getAllByCompanyId(Long companyId, Pageable pageable);

    Department getDepartmentById(Long id, Long companyId);

    DepartmentResponseDTO getDepartmentDTOById(Long id, Long companyId);

    DepartmentResponseDTO addDepartment(DepartmentRequestDTO department, Long companyId);

    void deleteDepartment(Long id, Long companyId);

    DepartmentResponseDTO updateDepartment(Long id,Long companyId, DepartmentRequestDTO updatedRequest);

    List<Long> getAllDepartmentIdsForCompany(Long companyId);

    DepartmentInfoDTO getDepartmentInfoById(Long id);
}