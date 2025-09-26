package com.timetrak.service.invite;

import com.timetrak.dto.invite.InviteCreateRequestDTO;
import com.timetrak.dto.invite.InviteResponseDTO;
import com.timetrak.dto.invite.InviteSignupRequestDTO;
import com.timetrak.dto.invite.InviteValidationResponseDTO;
import com.timetrak.dto.employee.EmployeeRequestDTO;
import com.timetrak.dto.employee.EmployeeResponseDTO;
import com.timetrak.entity.Company;
import com.timetrak.entity.Department;
import com.timetrak.entity.EmployeeInvite;
import com.timetrak.exception.ResourceNotFoundException;
import com.timetrak.repository.CompanyRepository;
import com.timetrak.repository.DepartmentRepository;
import com.timetrak.repository.EmployeeInviteRepository;
import com.timetrak.service.employee.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InviteServiceImpl implements InviteService {

    private final EmployeeInviteRepository inviteRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeService employeeService;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public InviteResponseDTO createInvite(InviteCreateRequestDTO request, Long createdByEmployeeId) {
        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            
            if (!department.getCompany().getId().equals(request.getCompanyId())) {
                throw new IllegalArgumentException("Department does not belong to the specified company");
            }
        }

        String inviteCode;
        do {
            inviteCode = UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        } while (inviteRepository.existsByInviteCode(inviteCode));

        LocalDateTime expiresAt = LocalDateTime.now().plusHours(request.getExpiryHours());

        EmployeeInvite invite = EmployeeInvite.builder()
                .inviteCode(inviteCode)
                .companyId(request.getCompanyId())
                .departmentId(request.getDepartmentId())
                .maxUses(request.getMaxUses())
                .currentUses(0)
                .expiresAt(expiresAt)
                .isActive(true)
                .createdByEmployeeId(createdByEmployeeId)
                .description(request.getDescription())
                .build();

        invite = inviteRepository.save(invite);



        log.info("Created invite with code: {} for company: {} by employee: {}", 
                inviteCode, request.getCompanyId(), createdByEmployeeId);

        InviteResponseDTO response =mapToResponseDTO(invite);

        response.setDepartmentName(
                Optional.ofNullable(department)
                        .map(Department::getName)
                        .orElse("Unknown Department")
        );
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public InviteValidationResponseDTO validateInvite(String inviteCode) {
        EmployeeInvite invite = inviteRepository.findActiveInviteByCode(inviteCode)
                .orElse(null);

        if (invite == null || !invite.isValid()) {
            return InviteValidationResponseDTO.builder()
                    .isValid(false)
                    .message("Invalid or expired invite code")
                    .build();
        }

        Company company = companyRepository.findById(invite.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        Department department = null;
        if (invite.getDepartmentId() != null) {
            department = departmentRepository.findById(invite.getDepartmentId()).orElse(null);
        }

        return InviteValidationResponseDTO.builder()
                .isValid(true)
                .message("Invite is valid")
                .companyId(invite.getCompanyId())
                .departmentId(invite.getDepartmentId())
                .companyName(company.getName())
                .departmentName(department != null ? department.getName() : null)
                .build();
    }

    @Override
    public EmployeeResponseDTO registerEmployeeWithInvite(InviteSignupRequestDTO request) {
        EmployeeInvite invite = inviteRepository.findActiveInviteByCode(request.getInviteCode())
                .orElseThrow(() -> new IllegalArgumentException("Invalid invite code"));

        if (!invite.isValid()) {
            throw new IllegalArgumentException("Invite code is expired or fully used");
        }

        // Create employee request DTO
        EmployeeRequestDTO employeeRequest = EmployeeRequestDTO.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(request.getPassword())
                .phoneNumber(request.getPhoneNumber())
                .username(request.getUsername())
                .companyId(invite.getCompanyId())
                .departmentId(invite.getDepartmentId())
                .build();

        // Create the employee
        EmployeeResponseDTO employee = employeeService.createEmployee(employeeRequest);

        // Increment invite usage
        invite.incrementUse();
        inviteRepository.save(invite);

        log.info("Employee registered with invite code: {} for company: {}", 
                request.getInviteCode(), invite.getCompanyId());

        return employee;
    }

    @Override
    @Transactional(readOnly = true)
    public InviteResponseDTO getInviteByCode(String inviteCode) {
        EmployeeInvite invite = inviteRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new ResourceNotFoundException("Invite not found"));

        return mapToResponseDTO(invite);
    }


    @Override
    @Transactional(readOnly = true)
    public List<InviteResponseDTO> getActiveInvitesByCompany(Long companyId) {
        List<EmployeeInvite> invites = inviteRepository.findByCompanyIdAndIsActiveTrue(companyId);
        return invites.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Override
    public InviteResponseDTO deactivateInvite(String inviteCode, Long companyId) {
        EmployeeInvite invite = inviteRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new ResourceNotFoundException("Invite not found"));

        if (!invite.getCompanyId().equals(companyId)) {
            throw new IllegalArgumentException("Invite does not belong to the specified company");
        }

        invite.setIsActive(false);
        invite = inviteRepository.save(invite);

        log.info("Deactivated invite with code: {} for company: {}", inviteCode, companyId);

        return mapToResponseDTO(invite);
    }

    @Override
    @Scheduled(fixedRate = 3600000) // Run every hour
    public void cleanupExpiredInvites() {
        List<EmployeeInvite> expiredInvites = inviteRepository.findExpiredInvites(LocalDateTime.now());
        for (EmployeeInvite invite : expiredInvites) {
            invite.setIsActive(false);
        }
        inviteRepository.saveAll(expiredInvites);
        
        if (!expiredInvites.isEmpty()) {
            log.info("Cleaned up {} expired invites", expiredInvites.size());
        }
    }

    @Override
    public String generateInviteUrl(String inviteCode) {
        return frontendUrl + "/register?invite=" + inviteCode;
    }

    private InviteResponseDTO mapToResponseDTO(EmployeeInvite invite) {
        return InviteResponseDTO.builder()
                .id(invite.getId())
                .inviteCode(invite.getInviteCode())
                .inviteUrl(generateInviteUrl(invite.getInviteCode()))
                .companyId(invite.getCompanyId())
                .departmentId(invite.getDepartmentId())
                .maxUses(invite.getMaxUses())
                .currentUses(invite.getCurrentUses())
                .isActive(invite.getIsActive())
                .expiresAt(invite.getExpiresAt())
                .createdAt(invite.getCreatedAt())
                .description(invite.getDescription())
                .isExpired(invite.isExpired())
                .isFullyUsed(invite.isFullyUsed())
                .isValid(invite.isValid())
                .build();
    }
}