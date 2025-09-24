package com.timetrak.service.invite;

import com.timetrak.dto.invite.InviteCreateRequestDTO;
import com.timetrak.dto.invite.InviteResponseDTO;
import com.timetrak.dto.invite.InviteSignupRequestDTO;
import com.timetrak.dto.invite.InviteValidationResponseDTO;
import com.timetrak.dto.employee.EmployeeResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InviteService {

    InviteResponseDTO createInvite(InviteCreateRequestDTO request, Long createdByEmployeeId);

    InviteValidationResponseDTO validateInvite(String inviteCode);

    EmployeeResponseDTO registerEmployeeWithInvite(InviteSignupRequestDTO request);

    InviteResponseDTO getInviteByCode(String inviteCode);

    Page<InviteResponseDTO> getInvitesByCompany(Long companyId, Pageable pageable);

    List<InviteResponseDTO> getActiveInvitesByCompany(Long companyId);

    InviteResponseDTO deactivateInvite(String inviteCode, Long companyId);

    void cleanupExpiredInvites();

    String generateInviteUrl(String inviteCode);
}