package com.timetrak.mapper;


import com.timetrak.dto.request.CompanyRequestDTO;
import com.timetrak.dto.response.CompanyResponseDTO;
import com.timetrak.entity.Company;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    Company toEntity(CompanyRequestDTO dto);

    CompanyResponseDTO toDTO(Company company);

    void updateCompanyFromDto(CompanyRequestDTO dto, @MappingTarget Company company);
}

