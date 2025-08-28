package com.timetrak.service.company;

import com.timetrak.dto.company.CompanyResponseDTO;
import com.timetrak.dto.company.CompanyRequestDTO;
import com.timetrak.entity.Company;
import com.timetrak.exception.DuplicateResourceException;
import com.timetrak.exception.ResourceNotFoundException;
import com.timetrak.mapper.CompanyMapper;
import com.timetrak.repository.CompanyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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




    @Override
    @Transactional
    public CompanyResponseDTO updateCompany(Long id, CompanyRequestDTO dto) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));
        validateUniqueCode(dto.getCode());
        companyMapper.updateCompanyFromDto(dto, company);
        Company updated = companyRepository.save(company);
        return companyMapper.toDTO(updated);
    }

    public void validateUniqueCode(String code){
        if(companyRepository.existsByCode(code)){
            throw new DuplicateResourceException("Company code already exists");
        }
    }


}
