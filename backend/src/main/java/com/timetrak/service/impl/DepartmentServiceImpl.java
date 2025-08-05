package com.timetrak.service.impl;

import com.timetrak.dto.response.DepartmentResponseDTO;
import com.timetrak.dto.request.DepartmentRequestDTO;
import com.timetrak.entity.Company;
import com.timetrak.entity.Department;
import com.timetrak.exception.ResourceNotFoundException;
import com.timetrak.exception.UnauthorizedAccessException;
import com.timetrak.mapper.DepartmentMapper;
import com.timetrak.repository.DepartmentRepository;
import com.timetrak.service.CompanyService;
import com.timetrak.service.DepartmentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final CompanyService companyService;
    private final DepartmentMapper departmentMapper;

    @Override
    public Department getDepartmentById(Long id, Long companyId) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        if (!department.getCompany().getId().equals(companyId)) {
            throw new UnauthorizedAccessException("Access denied: Department does not belong to your company.");
        }

        return department;
    }

    @Override
    public DepartmentResponseDTO getDepartmentDTOById(Long id, Long companyId) {
        Department department = getDepartmentById(id, companyId);
        return departmentMapper.toDTO(department);
    }

    @Override
    public Page<DepartmentResponseDTO> getAllByCompanyId(Long companyId, Pageable pageable) {
        return departmentRepository.findAllByCompanyId(companyId, pageable)
                .map(departmentMapper::toDTO);
    }

    @Override
    public DepartmentResponseDTO addDepartment(DepartmentRequestDTO request, Long companyId) {
        Company company = companyService.getCompanyById(companyId);

        Department department = Department.builder()
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .isActive(true)
                .company(company)
                .build();

        Department saved = departmentRepository.save(department);
        return departmentMapper.toDTO(saved);
    }

    @Override
    public void deleteDepartment(Long id, Long companyId) {
        Department department = getDepartmentById(id, companyId);
        department.markAsDeleted();
        departmentRepository.save(department);
    }


    @Override
    public DepartmentResponseDTO updateDepartment(Long id, Long companyId, DepartmentRequestDTO updatedRequest) {
        Department department = getDepartmentById(id, companyId);
        departmentMapper.updateDepartmentFromDto(updatedRequest,department);
        Department saved = departmentRepository.save(department);
        return departmentMapper.toDTO(saved);
    }

    @Override
    public DepartmentResponseDTO getDepartmentByCode(String code, Long companyId){
        Department department = departmentRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with code: " + code));
        if(!department.getCompany().getId().equals(companyId)){
            throw new UnauthorizedAccessException("Access denied");
        }
        return departmentMapper.toDTO(department);
    }

    @Override
    public boolean validateDepartmentBelongToCompany(Long departmentId, Long companyId) {
        if (departmentId == null || companyId == null) {
            return false;
        }
        return departmentRepository.existsByIdAndCompanyId(departmentId, companyId);
    }
}
