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
    public EmployeeResponseDTO getEmployeeDTOById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
        return employeeMapper.toDTO(employee);
    }

    @Override
    public Employee getById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
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
    public Employee getByUsername(String username) {
        return employeeRepository.findByUsername(username)
                .orElseThrow(() -> new EmployeeNotFoundException(username));
    }

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
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO dto) {
        Employee employee = getById(id);


        validationService.validateUpdate(employee,
                dto);

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
    public void deleteEmployee(Long id) {
        Employee employee = getById(id);


        validationService.validateDeletion(employee);

        employee.setStatus(EmployeeStatus.DELETED);
        employee.markAsDeleted();
        employeeRepository.save(employee);

        log.info("Deleted employee: {} (ID: {})", employee.getUsername(), employee.getId());
    }

    @Transactional
    @Override
    public void activateEmployee(Long id) {
        Employee  employee = getById(id);

        validationService.validateActivation(employee);

        employee.setStatus(EmployeeStatus.ACTIVE);
        employeeRepository.save(employee);

        log.info("Activated employee: {} (ID: {})", employee.getUsername(), employee.getId());
    }
    @Transactional
    @Override
    public void deactivateEmployee(Long id) {
        Employee  employee = getById(id);
        validationService.validateDeactivation(employee);
        employee.setStatus(EmployeeStatus.DEACTIVATED);
        employeeRepository.save(employee);

        log.info("Deactivated employee: {} (ID: {})", employee.getUsername(), employee.getId());
    }

    @Override
    @Transactional
    public void approveEmployee(Long id) {
        Employee  employee = getById(id);
        validationService.validateApproval(employee);

        employee.setStatus(EmployeeStatus.ACTIVE);
        employeeRepository.save(employee);
        log.info("Approved employee: {} (ID: {})", employee.getUsername(), employee.getId());
    }

    @Override
    @Transactional
    public void rejectEmployee(Long id) {
        Employee  employee = getById(id);
        validationService.validateRejection(employee);

        employee.setStatus(EmployeeStatus.REJECTED);
        employeeRepository.save(employee);
        log.info("Rejected employee: {} (ID: {})", employee.getUsername(), employee.getId());
    }

    @Override
    @Transactional
    public void requestReactivation(Long id) {
        Employee  employee = getById(id);

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
    public Page<EmployeeResponseDTO> getEmployeesByDepartment(Long departmentId, Pageable pageable) {
        validationService.validateDepartmentExists(departmentId);

        return employeeRepository.findByDepartmentIdActive(departmentId, pageable)
                .map(employeeMapper::toDTO);
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