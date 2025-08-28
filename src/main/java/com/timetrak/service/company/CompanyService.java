package com.timetrak.service.company;


import com.timetrak.dto.company.CompanyResponseDTO;
import com.timetrak.entity.Company;


import com.timetrak.dto.company.CompanyRequestDTO;

public interface CompanyService {

    Company getCompanyById(Long id);
    CompanyResponseDTO getCompanyDTOById(Long id);
    CompanyResponseDTO updateCompany(Long id, CompanyRequestDTO dto);



    // Utility
    boolean existsById(Long id);
}
