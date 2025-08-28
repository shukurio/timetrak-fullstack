package com.timetrak.controller;

import com.timetrak.security.auth.JwtService;
import com.timetrak.security.auth.dto.AuthRequest;
import com.timetrak.security.auth.dto.AuthResponse;
import com.timetrak.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication management endpoints")
public class AuthController {
    private final JwtService jwtService;
    private final AuthService authService;


    @GetMapping("/me/role")
    public ResponseEntity<String> getRoleFromToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String role = jwtService.extractRole(token);

        return ResponseEntity.ok("Role from token: " + role);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<String> adminEndpoint() {
        return ResponseEntity.ok("You are an admin!");
    }




    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request,
                                              HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request);

        //refresh token stored in HTTP-only cookie
        Cookie refreshCookie = new Cookie("refreshToken", authResponse.getRefreshToken());
        log.debug(authResponse.getRefreshToken());
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
    public ResponseEntity<AuthResponse> refreshToken(@CookieValue("refreshToken") String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        AuthResponse response = authService.refreshToken(refreshToken);
        // Again, don't expose refresh token in response
        response.setRefreshToken(null);
        return ResponseEntity.ok(response);
    }


//
//    @Operation(summary = "Refresh JWT token", description = "Get new access token using refresh token")
//    @PostMapping("/refresh")
//    public ResponseEntity<AuthResponse> refreshToken(@RequestBody Map<String, String> request) {
//        String refreshToken = request.get("refreshToken");
//        if (refreshToken == null || refreshToken.isEmpty()) {
//            return ResponseEntity.badRequest().build();
//        }
//
//        AuthResponse response = authService.refreshToken(refreshToken);
//        return ResponseEntity.ok(response);
//    }

    @Operation(summary = "Logout user", description = "Logout user and invalidate token")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader("Authorization") String authHeader,
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            Authentication authentication,
            HttpServletResponse response) {

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authService.logout(token);
        }

        // Clear the refresh token cookie
        Cookie refreshCookie = new Cookie("refreshToken", "");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setAttribute("SameSite", "Strict");
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0); // immediately expires the cookie
        response.addCookie(refreshCookie);

        log.info("User {} logged out", authentication != null ? authentication.getName() : "unknown");
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @Operation(summary = "Change password", description = "Change user password")
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        if (oldPassword == null || newPassword == null ||
                oldPassword.isEmpty() || newPassword.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Old password and new password are required"));
        }

        authService.changePassword(authentication.getName(), oldPassword, newPassword);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
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

    @Operation(summary = "Get current user info", description = "Get current authenticated user information")
    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(Map.of(
                "username", authentication.getName(),
                "authenticated", "true"
        ));
    }
}
