package com.timetrak.mapper;

import com.timetrak.dto.request.DepartmentRequestDTO;
import com.timetrak.dto.response.DepartmentResponseDTO;
import com.timetrak.entity.Department;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {
    Department toEntity(DepartmentRequestDTO dto);
    DepartmentResponseDTO toDTO(Department department);
    void updateDepartmentFromDto(DepartmentRequestDTO dto, @MappingTarget Department department);
}

