package com.timetrak.mapper;


import com.timetrak.dto.request.DepartmentRequestDTO;
import com.timetrak.dto.response.DepartmentResponseDTO;
import com.timetrak.entity.Company;
import com.timetrak.entity.Department;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    DepartmentMapper INSTANCE = Mappers.getMapper(DepartmentMapper.class);

    Company toEntity(DepartmentRequestDTO dto);

    DepartmentResponseDTO toDTO(Department department);

    void updateCompanyFromDto(DepartmentRequestDTO dto, @MappingTarget Department department);
}

