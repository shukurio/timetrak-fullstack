package com.timetrak.security.auth.dto;

import com.timetrak.dto.response.EmployeeResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String refreshToken;
    private String tokenType = "Bearer";
    private EmployeeResponseDTO user;
    private Long expiresIn;
}