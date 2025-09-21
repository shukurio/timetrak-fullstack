package com.timetrak.service.department;

import com.timetrak.dto.response.DepartmentResponseDTO;
import com.timetrak.dto.request.DepartmentRequestDTO;
import com.timetrak.entity.Company;
import com.timetrak.entity.Department;
import com.timetrak.exception.ResourceNotFoundException;
import com.timetrak.mapper.DepartmentMapper;
import com.timetrak.repository.DepartmentRepository;
import com.timetrak.service.company.CompanyService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final CompanyService companyService;
    private final DepartmentMapper departmentMapper;

    @Override
    public Department getDepartmentById(Long id, Long companyId) {

        return departmentRepository.findByIdAndCompanyIdAndDeletedAtIsNull(id,companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
    }

    @Override
    public DepartmentResponseDTO getDepartmentDTOById(Long id, Long companyId) {
        Department department = getDepartmentById(id, companyId);
        return departmentMapper.toDTO(department);
    }

    @Override
    public Page<DepartmentResponseDTO> getAllByCompanyId(Long companyId, Pageable pageable) {
        return departmentRepository.findActiveByCompanyId(companyId, pageable)
                .map(departmentMapper::toDTO);
    }

    @Override
    public DepartmentResponseDTO addDepartment(DepartmentRequestDTO request, Long companyId) {
        Company company = companyService.getCompanyById(companyId);

        Department department = Department.builder()
                .name(request.getName())
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
        department.setIsActive(false);
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
    public boolean validateDepartmentBelongToCompany(Long departmentId, Long companyId) {
        if (departmentId == null || companyId == null) {
            return false;
        }
        return departmentRepository.existsByIdAndCompanyId(departmentId, companyId);
    }

    @Override
    public List<Long> getAllDepartmentIdsForCompany(Long companyId) {
        return departmentRepository.findAllByCompanyIdAndIsActiveTrue(companyId)
                .stream()
                .map(Department::getId)
                .collect(Collectors.toList());
    }
}
