package com.timetrak.service.impl;


import com.timetrak.dto.DepartmentResponseDTO;
import com.timetrak.dto.request.EmployeeRequestDTO;
import com.timetrak.dto.EmployeeResponseDTO;
import com.timetrak.entity.Company;
import com.timetrak.entity.Department;
import com.timetrak.entity.Employee;
import com.timetrak.enums.Role;
import com.timetrak.exception.ResourceNotFoundException;
import com.timetrak.repository.EmployeeRepository;
import com.timetrak.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final DepartmentServiceImpl departmentServiceImpl;
    private final CompanyServiceImpl companyServiceImpl;


    @Override
    public EmployeeResponseDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        return mapToDTO(employee);
    }

    @Override
    public EmployeeResponseDTO getEmployeeByUsername(String username) {
        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with username: " + username));
        return mapToDTO(employee);
    }

    @Override
    public Page<EmployeeResponseDTO> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAllActiveAndEnabled(pageable)
                .map(this::mapToDTO);
    }

    @Override
    public List<EmployeeResponseDTO> getAllActiveEmployees() {
        return employeeRepository.findAllActiveAndEnabled()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
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

        return mapToDTO(employee);
    }

    @Override
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        employee.markAsDeleted();
        employeeRepository.save(employee);
        log.info("Deleted employee: {} (ID: {})", employee.getUsername(), employee.getId());
    }

    @Override
    public void activateEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        employee.setIsActive(true);
        employeeRepository.save(employee);
        log.info("Activated employee: {} (ID: {})", employee.getUsername(), employee.getId());
    }

    @Override
    public void deactivateEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        employee.setIsActive(false);
        employeeRepository.save(employee);
        log.info("Deactivated employee: {} (ID: {})", employee.getUsername(), employee.getId());
    }

    @Override
    public List<EmployeeResponseDTO> getActiveEmployees() {
        return employeeRepository.findAllActiveAndEnabled()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeResponseDTO> searchEmployees(String query) {
        return employeeRepository.searchActiveEmployees(query)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeResponseDTO> getEmployeesByDepartment(Long departmentId) {
        return employeeRepository.findByDepartmentId(departmentId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }


    @Override
    public EmployeeResponseDTO registerEmployee(EmployeeRequestDTO dto) {
        // Note: Simplified for now, job relationships can be added later if needed

        Department department = departmentServiceImpl.getDepartmentById(dto.getDepartmentId());
        Company company = companyServiceImpl.getCompanyById(dto.getCompanyId());
        Employee employee = Employee.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(Role.EMPLOYEE)
                .department(department)
                .company(company)
                .isActive(true)
                .build();

        employee = employeeRepository.save(employee);
        log.info("Registered new employee: {} (ID: {})", employee.getUsername(), employee.getId());

        return mapToDTO(employee);
    }



    @Override
    public Optional<Employee> findByUsername(String username) {
        return employeeRepository.findByUsername(username);
    }

    private EmployeeResponseDTO mapToDTO(Employee employee) {
        return EmployeeResponseDTO.builder()
                .id(employee.getId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .username(employee.getUsername())
                .email(employee.getEmail())
                .role(employee.getRole())
                .phoneNumber(employee.getPhoneNumber())
                .isActive(employee.getIsActive())
                .departmentId(employee.getDepartment().getId())
                .companyId(employee.getCompany().getId())
                .build();
    }

    private DepartmentResponseDTO mapDepartmentToDTO(Department department) {
        if (department == null) {
            return null;
        }
        return DepartmentResponseDTO.builder()
                .id(department.getId())
                .name(department.getName())
                .code(department.getCode())
                .description(department.getDescription())
                .isActive(department.getIsActive())
                .build();
    }

}

