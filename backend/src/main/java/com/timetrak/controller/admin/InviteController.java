package com.timetrak.controller.admin;

import com.timetrak.dto.invite.InviteCreateRequestDTO;
import com.timetrak.dto.invite.InviteResponseDTO;
import com.timetrak.service.auth.AuthContextService;
import com.timetrak.service.invite.InviteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/invites")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class InviteController {

    private final InviteService inviteService;
    private final AuthContextService authContextService;

    @PostMapping
    public ResponseEntity<InviteResponseDTO> createInvite(@Valid @RequestBody InviteCreateRequestDTO request) {
        Long currentEmployeeId = authContextService.getCurrentEmployeeId();
        Long currentCompanyId = authContextService.getCurrentCompanyId();
        
        // Ensure the request is for the current user's company
        request.setCompanyId(currentCompanyId);
        
        InviteResponseDTO response = inviteService.createInvite(request, currentEmployeeId);
        
        log.info("Invite created with code: {} by employee: {}", response.getInviteCode(), currentEmployeeId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{inviteCode}")
    public ResponseEntity<InviteResponseDTO> getInviteByCode(@PathVariable String inviteCode) {
        Long currentCompanyId = authContextService.getCurrentCompanyId();
        InviteResponseDTO response = inviteService.getInviteByCode(inviteCode);
        
        // Ensure the invite belongs to the current user's company
        if (!response.getCompanyId().equals(currentCompanyId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(response);
    }


    @GetMapping()
    public ResponseEntity<List<InviteResponseDTO>> getActiveInvites() {
        Long currentCompanyId = authContextService.getCurrentCompanyId();
        List<InviteResponseDTO> response = inviteService.getActiveInvitesByCompany(currentCompanyId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{inviteCode}/deactivate")
    public ResponseEntity<InviteResponseDTO> deactivateInvite(@PathVariable String inviteCode) {
        Long currentCompanyId = authContextService.getCurrentCompanyId();
        InviteResponseDTO response = inviteService.deactivateInvite(inviteCode, currentCompanyId);
        
        log.info("Invite deactivated: {} by company: {}", inviteCode, currentCompanyId);
        
        return ResponseEntity.ok(response);
    }

}