package com.timetrak.controller.admin;

import com.timetrak.dto.company.CompanyRegistrationDTO;
import com.timetrak.dto.company.CompanyRegistrationResponseDTO;


import com.timetrak.service.company.CompanyRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/companies")
@Slf4j
public class CompanyRegistrationController {
    private final CompanyRegistrationService companyRegistrationService;


    @PostMapping("/register")
    public ResponseEntity<CompanyRegistrationResponseDTO> createCompany(@Valid @RequestBody CompanyRegistrationDTO request) {
        CompanyRegistrationResponseDTO response =
                companyRegistrationService.createCompany(request);

        return ResponseEntity.ok(response);
    }
}
