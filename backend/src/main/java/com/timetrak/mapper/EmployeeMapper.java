package com.timetrak.mapper;

import com.timetrak.dto.request.EmployeeRequestDTO;
import com.timetrak.dto.response.EmployeeResponseDTO;
import com.timetrak.entity.Company;
import com.timetrak.entity.Department;
import com.timetrak.entity.Employee;
import com.timetrak.enums.Role;
import com.timetrak.service.CompanyService;
import com.timetrak.service.DepartmentService;
import org.mapstruct.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    // DTO Mapping (Simple - no dependencies needed)
    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "companyId", source = "company.id")
    EmployeeResponseDTO toDTO(Employee employee);

    // Entity Mapping (Complex - needs services)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "company", source = "dto", qualifiedByName = "mapCompany")
    @Mapping(target = "department", source = "dto", qualifiedByName = "mapDepartment")
    @Mapping(target = "password", source = "dto", qualifiedByName = "encodePassword")
    @Mapping(target = "role", source = "dto", qualifiedByName = "setDefaultRole")
    @Mapping(target = "status", constant = "PENDING")
    Employee toEntity(EmployeeRequestDTO dto,
                      @Context CompanyService companyService,
                      @Context DepartmentService departmentService,
                      @Context PasswordEncoder passwordEncoder);

    // Custom mapping methods
    @Named("mapCompany")
    default Company mapCompany(EmployeeRequestDTO dto, @Context CompanyService companyService) {
        return companyService.getCompanyById(dto.getCompanyId());
    }

    @Named("mapDepartment")
    default Department mapDepartment(EmployeeRequestDTO dto, @Context DepartmentService departmentService) {
        return departmentService.getDepartmentById(dto.getDepartmentId(), dto.getCompanyId());
    }

    @Named("encodePassword")
    default String encodePassword(EmployeeRequestDTO dto, @Context PasswordEncoder passwordEncoder) {
        return dto.getPassword() != null ? passwordEncoder.encode(dto.getPassword()) : null;
    }

    @Named("setDefaultRole")
    default Role setDefaultRole(EmployeeRequestDTO dto) {
        return dto.getRole() != null ? dto.getRole() : Role.EMPLOYEE;
    }
}