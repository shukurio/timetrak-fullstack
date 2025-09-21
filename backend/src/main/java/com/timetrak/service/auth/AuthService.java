package com.timetrak.service.auth;

import com.timetrak.security.auth.dto.AuthRequest;
import com.timetrak.security.auth.dto.AuthResponse;

public interface AuthService {
    AuthResponse login(AuthRequest request);
    void logout(String token);
    AuthResponse refreshToken(String refreshToken);
    void changePassword(String username, String oldPassword, String newPassword);
    void resetPassword(String email);
}
