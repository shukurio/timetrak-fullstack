package com.timetrak.controller.user;

import com.timetrak.dto.invite.InviteSignupRequestDTO;
import com.timetrak.dto.invite.InviteValidationResponseDTO;
import com.timetrak.dto.response.EmployeeResponseDTO;
import com.timetrak.service.invite.InviteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/invites")
@RequiredArgsConstructor
@Slf4j
public class UserInviteController {

    private final InviteService inviteService;

    @GetMapping("/validate/{inviteCode}")
    public ResponseEntity<InviteValidationResponseDTO> validateInvite(@PathVariable String inviteCode) {
        InviteValidationResponseDTO response = inviteService.validateInvite(inviteCode);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<EmployeeResponseDTO> registerWithInvite(@Valid @RequestBody InviteSignupRequestDTO request) {
        EmployeeResponseDTO response = inviteService.registerEmployeeWithInvite(request);
        
        log.info("Employee registered with invite code: {}", request.getInviteCode());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/url/{inviteCode}")
    public ResponseEntity<String> getInviteUrl(@PathVariable String inviteCode) {
        String inviteUrl = inviteService.generateInviteUrl(inviteCode);
        return ResponseEntity.ok(inviteUrl);
    }
}