package com.timetrak.service.company;

import com.timetrak.dto.company.CompanyRegistrationDTO;
import com.timetrak.dto.company.CompanyRegistrationResponseDTO;
import com.timetrak.dto.company.CompanyResponseDTO;
import com.timetrak.dto.employee.EmployeeResponseDTO;
import com.timetrak.entity.Company;
import com.timetrak.exception.DuplicateResourceException;
import com.timetrak.mapper.CompanyMapper;
import com.timetrak.repository.CompanyRepository;
import com.timetrak.service.employee.EmployeeService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Transactional
public class CompanyRegistrationServiceImpl implements CompanyRegistrationService {
    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final EmployeeService employeeService;

    @Transactional
    @Override
    public CompanyRegistrationResponseDTO createCompany(CompanyRegistrationDTO request) {


        //IsActive should be fixed when Sys-Admin is implemented
        if(request.getCompany().getCode() == null){
            throw new IllegalArgumentException("Company code is null");

        }
        validateUniqueCode(request.getCompany().getCode());
        Company company = companyRepository.save(companyMapper.toEntity(request.getCompany()));
        CompanyResponseDTO companyDto = companyMapper.toDTO(company);


        EmployeeResponseDTO employee = employeeService.createAdmin(request.getAdmin(),company);

        return new CompanyRegistrationResponseDTO(companyDto,employee);

    }



    public void validateUniqueCode(String code){
        if(companyRepository.existsByCode(code)){
            throw new DuplicateResourceException("Company code already exists");
        }
    }
}
