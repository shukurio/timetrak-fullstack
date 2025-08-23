package com.timetrak.service.employee;


import com.timetrak.dto.request.EmployeeRequestDTO;
import com.timetrak.dto.response.EmployeeResponseDTO;
import com.timetrak.entity.Employee;
import com.timetrak.enums.EmployeeStatus;
import com.timetrak.exception.employee.DuplicateEmployeeException;
import com.timetrak.exception.employee.EmployeeNotFoundException;
import com.timetrak.exception.employee.EmployeeValidationException;
import com.timetrak.mapper.EmployeeMapper;
import com.timetrak.repository.EmployeeRepository;
import com.timetrak.service.CompanyService;
import com.timetrak.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final DepartmentService departmentService;
    private final CompanyService companyService;
    private final EmployeeMapper employeeMapper;
    private final EmployeeValidationService validationService;

    @Override
    public EmployeeResponseDTO getEmployeeDTOById(Long id, Long companyId) {
        Employee employee = employeeRepository.findByIdAndCompanyIdAndDeletedAtIsNull(id, companyId)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
        return employeeMapper.toDTO(employee);
    }
    
    // Legacy method (keeping for backward compatibility - NO company scope)
    @Override
    public EmployeeResponseDTO getEmployeeDTOById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
        return employeeMapper.toDTO(employee);
    }

    @Override
    public Employee getById(Long employeeId, Long companyId) {
        return employeeRepository.findByIdAndCompanyIdAndDeletedAtIsNull(employeeId, companyId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
    }

    @Override
    public Employee getActiveById(Long employeeId, Long companyId) {
        return employeeRepository.findActiveByIdAndCompanyId(employeeId, companyId)
                .orElseThrow(() -> new EmployeeNotFoundException("Active employee not found with id: " + employeeId));
    }

    @Override
    public List<Employee> getByIds(List<Long> ids, Long companyId) {
        return employeeRepository.findAllByIdAndCompanyIdDeletedIncluded(ids, companyId);
    }

    //for company
    @Override
    public Page<EmployeeResponseDTO> getAllActiveForCompany(Long companyId,Pageable pageable) {
        return employeeRepository.findAllActiveByCompanyId(companyId,pageable)
                .map(employeeMapper::toDTO);

    }


    @Override
    public EmployeeResponseDTO getByUsername(String username, Long companyId) {
        Employee employee = employeeRepository.findByUsernameAndCompanyId(username, companyId)
                .orElseThrow(() -> new EmployeeNotFoundException(username));
        return employeeMapper.toDTO(employee);
    }
    
    // Legacy method (keeping for backward compatibility - NO company scope)
    @Override
    public EmployeeResponseDTO getByUsername(String username) {
        Employee employee = employeeRepository.findActiveByUsername(username)
                .orElseThrow(() -> new EmployeeNotFoundException(username));
        return employeeMapper.toDTO(employee);
    }

    @Override
    public Employee getByEmail(String email, Long companyId) {
        return employeeRepository.findByEmailAndCompanyId(email, companyId)
                .orElseThrow(() -> new EmployeeNotFoundException(email));
    }
    
    // Legacy method (keeping for backward compatibility - NO company scope)
    @Override
    public Employee getByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new EmployeeNotFoundException(email));
    }

    @Override
    public Page<EmployeeResponseDTO> getAllEmployeesForCompany(Long companyId,Pageable pageable) {
        return employeeRepository.findAllByCompanyId(companyId, pageable)
                .map(employeeMapper::toDTO);
    }





    @Transactional
    @Override
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO dto, Long companyId) {
        Employee employee = getById(id, companyId);

        validationService.validateUpdate(employee, dto);

        try {
            updateEmployeeFields(employee, dto);
            employee = employeeRepository.save(employee);

            log.info("Updated employee: {} (ID: {})", employee.getUsername(), employee.getId());
            return employeeMapper.toDTO(employee);

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while updating employee {}: {}", id, e.getMessage());
            handleDataIntegrityViolation(e, employee.getUsername(), employee.getEmail());
            throw new EmployeeValidationException("Unexpected data integrity violation", e);
        }
    }

    @Transactional
    @Override
    public void deleteEmployee(Long id, Long companyId) {
        Employee employee = getById(id, companyId);

        validationService.validateDeletion(employee);

        employee.setStatus(EmployeeStatus.DELETED);
        employee.markAsDeleted();
        employeeRepository.save(employee);

        log.info("Deleted employee: {} (ID: {})", employee.getUsername(), employee.getId());
    }

    @Transactional
    @Override
    public void activateEmployee(Long id, Long companyId) {
        Employee employee = getById(id, companyId);

        validationService.validateActivation(employee);

        employee.setStatus(EmployeeStatus.ACTIVE);
        employeeRepository.save(employee);

        log.info("Activated employee: {} (ID: {})", employee.getUsername(), employee.getId());
    }
    
    @Transactional
    @Override
    public void deactivateEmployee(Long id, Long companyId) {
        Employee employee = getById(id, companyId);
        validationService.validateDeactivation(employee);
        employee.setStatus(EmployeeStatus.DEACTIVATED);
        employeeRepository.save(employee);

        log.info("Deactivated employee: {} (ID: {})", employee.getUsername(), employee.getId());
    }

    @Override
    @Transactional
    public void approveEmployee(Long id, Long companyId) {
        Employee employee = getById(id, companyId);
        validationService.validateApproval(employee);

        employee.setStatus(EmployeeStatus.ACTIVE);
        employeeRepository.save(employee);
        log.info("Approved employee: {} (ID: {})", employee.getUsername(), employee.getId());
    }

    @Override
    @Transactional
    public void rejectEmployee(Long id, Long companyId) {
        Employee employee = getById(id, companyId);
        validationService.validateRejection(employee);

        employee.setStatus(EmployeeStatus.REJECTED);
        employeeRepository.save(employee);
        log.info("Rejected employee: {} (ID: {})", employee.getUsername(), employee.getId());
    }

    @Override
    @Transactional
    public void requestReactivation(Long id, Long companyId) {
        Employee employee = getById(id, companyId);

        validationService.validateReactivation(employee);

        employee.setStatus(EmployeeStatus.PENDING);
        employeeRepository.save(employee);
        log.info("Employee requested reactivation: {} (ID: {})", employee.getUsername(), employee.getId());
    }

    @Override
    public Page<EmployeeResponseDTO> getByStatus(Long companyId,EmployeeStatus status, Pageable pageable) {
        return employeeRepository.findAllByCompanyIdAndStatusPaged(companyId, status, pageable)
                .map(employeeMapper::toDTO);
    }


    @Override
    public Page<EmployeeResponseDTO> searchEmployees(String query,Long companyId, Pageable pageable) {
        validationService.validateSearchQuery(query);

        return employeeRepository.searchActiveEmployees(query,companyId, pageable)
                .map(employeeMapper::toDTO);
    }

    @Override
    public Page<EmployeeResponseDTO> getEmployeesByDepartment(Long departmentId, Long companyId, Pageable pageable) {
        log.debug("Getting employees for department ID: {}", departmentId);
        
        // Validate department exists
        validationService.validateDepartmentExists(departmentId);

        Page<Employee> employees = employeeRepository.findByDepartmentIdActive(departmentId, companyId, pageable);
        log.debug("Found {} employees for department ID: {}", employees.getTotalElements(), departmentId);
        
        return employees.map(employeeMapper::toDTO);
    }

    @Transactional
    @Override
    public EmployeeResponseDTO registerEmployee(EmployeeRequestDTO dto) {
        try {
            validationService.validateRegistration(dto);

            Employee employee = employeeMapper.toEntity(dto, companyService, departmentService, passwordEncoder);
            employee = employeeRepository.save(employee);

            log.info("Registered new employee: {} (ID: {})", employee.getUsername(), employee.getId());
            return employeeMapper.toDTO(employee);

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while registering employee: {}", e.getMessage());
            handleDataIntegrityViolation(e, dto.getUsername(), dto.getEmail());
            throw new EmployeeValidationException("Unexpected data integrity violation", e);
        }
    }

    @Override
    public String getEmployeeNameById(Long id, Long companyId) {
        return employeeRepository.findFullNameByIdAndCompanyId(id, companyId)
                .orElse("Employee " + id);
    }
    
    // Legacy method (keeping for backward compatibility - NO company scope)
    @Override
    public String getEmployeeNameById(Long id) {
        return employeeRepository.findFullNameById(id)
                .orElse("Employee " + id);
    }


    // ========== HELPER METHODS ==========

    private void updateEmployeeFields(Employee employee, EmployeeRequestDTO dto) {
        if (dto.getFirstName() != null) {
            employee.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null) {
            employee.setLastName(dto.getLastName());
        }
        if (dto.getEmail() != null) {
            employee.setEmail(dto.getEmail());
        }
        if (dto.getPhoneNumber() != null) {
            employee.setPhoneNumber(dto.getPhoneNumber());
        }

        if(dto.getUsername()!=null) {
            employee.setUsername(dto.getUsername());
        }
    }

    private void handleDataIntegrityViolation(DataIntegrityViolationException e,
                                              String username, String email) {
        if (e.getMessage().contains("username")) {
            throw new DuplicateEmployeeException("username", username);
        } else if (e.getMessage().contains("email")) {
            throw new DuplicateEmployeeException("email", email);
        }
    }

    //============Internal Use, return List===========

    @Override
    public List<Long> getAllActiveEmployeeIdsForCompany(Long companyId) {
        return employeeRepository.findActiveEmployeeIdsByCompanyId(companyId);
    }
}