package com.timetrak.mapper;

import com.timetrak.dto.employee.EmployeeRequestDTO;
import com.timetrak.dto.employee.EmployeeResponseDTO;
import com.timetrak.entity.Employee;
import org.mapstruct.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "companyId", source = "company.id")
    @Mapping(target = "departmentName", source ="department.name")
    EmployeeResponseDTO toDTO(Employee employee);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "company", ignore = true)  // Handle in service
    @Mapping(target = "department", ignore = true)  // Handle in service
    @Mapping(target = "password", source = "dto", qualifiedByName = "encodePassword")
    @Mapping(target = "status", constant = "PENDING")
    Employee toEntity(EmployeeRequestDTO dto, @Context PasswordEncoder passwordEncoder);

    // Only keep password encoding (no circular dependency)
    @Named("encodePassword")
    default String encodePassword(EmployeeRequestDTO dto, @Context PasswordEncoder passwordEncoder) {
        return dto.getPassword() != null ? passwordEncoder.encode(dto.getPassword()) : null;
    }
}