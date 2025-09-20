package com.timetrak.service.company;


import com.timetrak.dto.company.CompanyResponseDTO;
import com.timetrak.dto.company.CompanyUpdateDTO;
import com.timetrak.entity.Company;



public interface CompanyService {

    Company getCompanyById(Long id);
    CompanyResponseDTO getCompanyDTOById(Long id);
    CompanyResponseDTO updateCompany(Long id, CompanyUpdateDTO dto);
}
