package com.timetrak.mapper;


import com.timetrak.dto.company.CompanyRequestDTO;
import com.timetrak.dto.company.CompanyResponseDTO;
import com.timetrak.dto.company.CompanyUpdateDTO;
import com.timetrak.entity.Company;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",  nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CompanyMapper {

    Company toEntity(CompanyRequestDTO dto);

    CompanyResponseDTO toDTO(Company company);

    void updateCompanyFromDto(CompanyUpdateDTO dto, @MappingTarget Company company);
}

