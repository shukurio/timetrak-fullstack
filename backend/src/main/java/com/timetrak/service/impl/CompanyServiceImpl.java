package com.timetrak.service.impl;

import com.timetrak.dto.response.CompanyResponseDTO;
import com.timetrak.dto.request.CompanyRequestDTO;
import com.timetrak.entity.Company;
import com.timetrak.exception.ResourceNotFoundException;
import com.timetrak.mapper.CompanyMapper;
import com.timetrak.repository.CompanyRepository;
import com.timetrak.service.CompanyService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Transactional
@Service
public class CompanyServiceImpl implements CompanyService {
    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;


    @Override
    public Company getCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));
    }


    @Override
    public boolean existsById(Long id) {
        return companyRepository.existsById(id);
    }


    @Override
    public CompanyResponseDTO getCompanyDTOById(Long id) {
         Company company = companyRepository.findById(id)
                 .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));
         return companyMapper.toDTO(company);
    }

    //SYSADMIN-ONLY
    @Override
    public Page<CompanyResponseDTO> getAllCompanies(Pageable pageable) {
        return companyRepository.findAll(pageable).map(companyMapper::toDTO) ;
    }

    //SYSADMIN-ONLY
    @Override
    public Page<CompanyResponseDTO> getActiveCompanies(Pageable pageable) {
        return companyRepository.findAllActive(pageable).map(companyMapper::toDTO);
    }

    @Override
    public CompanyResponseDTO createCompany(CompanyRequestDTO dto) {
        Company company = companyMapper.toEntity(dto);
        Company saved  = companyRepository.save(company);
        return companyMapper.toDTO(saved);
    }

    @Override
    public CompanyResponseDTO updateCompany(Long id, CompanyRequestDTO dto) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));
        
        companyMapper.updateCompanyFromDto(dto, company);
        Company updated = companyRepository.save(company);
        return companyMapper.toDTO(updated);
    }


}
