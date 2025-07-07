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
    public EmployeeResponseDTO getEmployeeDTOByUsername(String username) {
        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new EmployeeNotFoundException(username));
        return employeeMapper.toDTO(employee);
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
    public Page<EmployeeResponseDTO> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable)
                .map(employeeMapper::toDTO);
    }

    @Override
    public Page<EmployeeResponseDTO> getAllActiveEmployees(Pageable pageable) {
        return employeeRepository.findAllActive(pageable)
                .map(employeeMapper::toDTO);
    }

    @Transactional
    @Override
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));


        validationService.validateUpdate(employee,
                dto);

        try {
            updateEmployeeFields(employee, dto);
            employee = employeeRepository.save(employee);

            log.info("Updated employee: {} (ID: {})", employee.getUsername(), employee.getId());
            return employeeMapper.toDTO(employee);

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while updating employee {}: {}", id, e.getMessage());
            handleDataIntegrityViolation(e, dto, employee);
            throw new EmployeeValidationException("Unexpected data integrity violation", e);
        }
    }

    @Transactional
    @Override
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));


        validationService.validateDeletion(employee);

        employee.setStatus(EmployeeStatus.DELETED);
        employee.markAsDeleted();
        employeeRepository.save(employee);

        log.info("Deleted employee: {} (ID: {})", employee.getUsername(), employee.getId());
    }

    @Transactional
    @Override
    public void activateEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        validationService.validateActivation(employee);

        employee.setStatus(EmployeeStatus.ACTIVE);
        employeeRepository.save(employee);

        log.info("Activated employee: {} (ID: {})", employee.getUsername(), employee.getId());
    }
    @Transactional
    @Override
    public void deactivateEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));


        validationService.validateDeactivation(employee);

        employee.setStatus(EmployeeStatus.DEACTIVATED);
        employeeRepository.save(employee);

        log.info("Deactivated employee: {} (ID: {})", employee.getUsername(), employee.getId());
    }


    @Override
    public Page<EmployeeResponseDTO> searchEmployees(String query, Pageable pageable) {
        validationService.validateSearchQuery(query);

        return employeeRepository.searchActiveEmployees(query, pageable)
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
            handleRegistrationDataIntegrityViolation(e, dto);
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
    }

    private void handleDataIntegrityViolation(DataIntegrityViolationException e,
                                              EmployeeRequestDTO dto,
                                              Employee employee) {
        if (e.getMessage().contains("email")) {
            throw new DuplicateEmployeeException("email", dto.getEmail());
        } else if (e.getMessage().contains("username")) {
            throw new DuplicateEmployeeException("username", employee.getUsername());
        }
    }

    private void handleRegistrationDataIntegrityViolation(DataIntegrityViolationException e,
                                                          EmployeeRequestDTO dto) {
        if (e.getMessage().contains("username")) {
            throw new DuplicateEmployeeException("username", dto.getUsername());
        } else if (e.getMessage().contains("email")) {
            throw new DuplicateEmployeeException("email", dto.getEmail());
        }
    }
}