package com.timetrak.service.impl;


import com.timetrak.dto.request.EmployeeRequestDTO;
import com.timetrak.dto.response.EmployeeResponseDTO;
import com.timetrak.entity.Employee;
import com.timetrak.enums.EmployeeStatus;
import com.timetrak.exception.ResourceNotFoundException;
import com.timetrak.mapper.EmployeeMapper;
import com.timetrak.repository.EmployeeRepository;
import com.timetrak.service.CompanyService;
import com.timetrak.service.DepartmentService;
import com.timetrak.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final DepartmentService departmentService;
    private final CompanyService companyService;
    private final EmployeeMapper employeeMapper;


    @Override
    public EmployeeResponseDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        return employeeMapper.toDTO(employee);
    }

    @Override
    public EmployeeResponseDTO getEmployeeByUsername(String username) {
        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with username: " + username));
        return employeeMapper.toDTO(employee);
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

    @Override
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeResponseDTO EmployeeResponseDTO) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        // Update fields
        if (EmployeeResponseDTO.getFirstName() != null) {
            employee.setFirstName(EmployeeResponseDTO.getFirstName());
        }
        if (EmployeeResponseDTO.getLastName() != null) {
            employee.setLastName(EmployeeResponseDTO.getLastName());
        }
        if (EmployeeResponseDTO.getEmail() != null) {
            employee.setEmail(EmployeeResponseDTO.getEmail());
        }

        if (EmployeeResponseDTO.getPhoneNumber() != null) {
            employee.setPhoneNumber(EmployeeResponseDTO.getPhoneNumber());
        }

        employee = employeeRepository.save(employee);
        log.info("Updated employee: {} (ID: {})", employee.getUsername(), employee.getId());

        return employeeMapper.toDTO(employee);
    }

    @Override
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        employee.setStatus(EmployeeStatus.DELETED);
        employee.markAsDeleted();

        employeeRepository.save(employee);
        log.info("Deleted employee: {} (ID: {})", employee.getUsername(), employee.getId());
    }

    @Override
    public void activateEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        employee.setStatus(EmployeeStatus.ACTIVE);
        employeeRepository.save(employee);
        log.info("Activated employee: {} (ID: {})", employee.getUsername(), employee.getId());
    }

    @Override
    public void deactivateEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        employee.setStatus(EmployeeStatus.INACTIVE);
        employeeRepository.save(employee);
        log.info("Deactivated employee: {} (ID: {})", employee.getUsername(), employee.getId());
    }



    @Override
    public Page<EmployeeResponseDTO> searchEmployees(String query,Pageable pageable) {
        return employeeRepository.searchActiveEmployees(query, pageable)
                .map(employeeMapper::toDTO);

    }

    @Override
    public Page<EmployeeResponseDTO> getEmployeesByDepartment(Long departmentId,Pageable pageable) {
        return employeeRepository.findByDepartmentIdActive(departmentId, pageable)
                .map(employeeMapper::toDTO);
    }


    @Override
    public EmployeeResponseDTO registerEmployee(EmployeeRequestDTO dto) {
        Employee employee = employeeMapper.toEntity(dto,
                companyService,
                departmentService,
                passwordEncoder);
        employee = employeeRepository.save(employee);

        log.info("Registered new employee: {} (ID: {})", employee.getUsername(), employee.getId());

        return employeeMapper.toDTO(employee);
    }


    @Override
    public Optional<Employee> findByUsername(String username) {
        return employeeRepository.findByUsername(username);
    }

    @Override
    public String getEmployeeNameById(Long id) {
        return employeeRepository.findFullNameById(id)
                .orElse("Employee " + id);
    }



}

