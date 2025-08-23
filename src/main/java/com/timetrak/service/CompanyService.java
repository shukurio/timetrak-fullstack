package com.timetrak.service;

import com.timetrak.dto.response.CompanyResponseDTO;
import com.timetrak.entity.Company;


import com.timetrak.dto.request.CompanyRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CompanyService {

    Company getCompanyById(Long id);
    CompanyResponseDTO getCompanyDTOById(Long id);

    Page<CompanyResponseDTO> getAllCompanies(Pageable pageable);

    // Only active companies (optional use case)
    Page<CompanyResponseDTO> getActiveCompanies(Pageable pageable);

    CompanyResponseDTO createCompany(CompanyRequestDTO dto);
    CompanyResponseDTO updateCompany(Long id, CompanyRequestDTO dto);


    // Utility
    boolean existsById(Long id);
}
