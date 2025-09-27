package com.timetrak.controller.unsecured;

import com.timetrak.dto.company.CompanyRegistrationDTO;
import com.timetrak.dto.company.CompanyRegistrationResponseDTO;
import com.timetrak.security.auth.dto.AuthRequest;
import com.timetrak.security.auth.dto.AuthResponse;
import com.timetrak.service.auth.AuthService;
import com.timetrak.service.company.CompanyRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;
    private final CompanyRegistrationService companyRegistrationService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request,
                                              HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request);

        //refresh token stored in HTTP-only cookie
        Cookie refreshCookie = new Cookie("refreshToken", authResponse.getRefreshToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setAttribute("SameSite", "Strict");
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(24 * 60 * 60);
        response.addCookie(refreshCookie);

        //no refresh token in response body
        authResponse.setRefreshToken(null);

        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@CookieValue (value = "refreshToken",  required = false) String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        AuthResponse response = authService.refreshToken(refreshToken);
        response.setRefreshToken(null);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Logout user", description = "Logout user and invalidate token")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader("Authorization") String authHeader,
            HttpServletResponse response) {

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authService.logout(token);
        }

        Cookie refreshCookie = new Cookie("refreshToken", "");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setAttribute("SameSite", "Strict");
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0); // immediately expires the cookie
        response.addCookie(refreshCookie);

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @Operation(summary = "Reset password", description = "Request password reset via email")
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email is required"));
        }

        try {
            authService.resetPassword(email);
            return ResponseEntity.ok(Map.of("message", "Password reset instructions sent to email"));
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body(Map.of("error", "Password reset functionality not yet implemented"));
        }
    }

    @PostMapping("/register/company")
    public ResponseEntity<CompanyRegistrationResponseDTO> createCompany(@Valid @RequestBody CompanyRegistrationDTO request) {
        CompanyRegistrationResponseDTO response =
                companyRegistrationService.createCompany(request);

        return ResponseEntity.ok(response);
    }




}
