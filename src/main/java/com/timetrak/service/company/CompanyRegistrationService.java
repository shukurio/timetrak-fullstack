package com.timetrak.service.company;

import com.timetrak.dto.company.CompanyRegistrationDTO;
import com.timetrak.dto.company.CompanyRegistrationResponseDTO;


public interface CompanyRegistrationService {

    CompanyRegistrationResponseDTO createCompany(CompanyRegistrationDTO request);
}
