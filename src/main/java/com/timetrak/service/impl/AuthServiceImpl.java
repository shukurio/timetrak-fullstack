package com.timetrak.service.impl;


import com.timetrak.dto.request.EmployeeRequestDTO;
import com.timetrak.dto.response.DepartmentResponseDTO;
import com.timetrak.dto.response.EmployeeResponseDTO;
import com.timetrak.entity.Department;
import com.timetrak.entity.Employee;
import com.timetrak.enums.EmployeeStatus;
import com.timetrak.exception.DuplicateResourceException;
import com.timetrak.exception.InvalidCredentialsException;
import com.timetrak.exception.ResourceNotFoundException;
import com.timetrak.exception.TokenExpiredException;
import com.timetrak.repository.DepartmentRepository;
import com.timetrak.repository.EmployeeRepository;
import com.timetrak.security.auth.CustomUserDetails;
import com.timetrak.security.auth.JwtService;
import com.timetrak.security.auth.dto.AuthRequest;
import com.timetrak.security.auth.dto.AuthResponse;
import com.timetrak.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse login(AuthRequest request) {
        try {
            // Authenticate user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // Get user details
            Employee employee = employeeRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found with username: " + request.getUsername()));

            if (!EmployeeStatus.ACTIVE.equals(employee.getStatus()))
                {
                //TODO Dont hARdcode
                throw new InvalidCredentialsException("Account is deactivated");
            }



            UserDetails userDetails = new CustomUserDetails(employee);
            String accessToken = jwtService.generateToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            log.info("User {} logged in successfully", request.getUsername());

            return AuthResponse.builder()
                    .token(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtService.getExpirationTime())
                    .user(mapToEmployeeDTO(employee))
                    .build();

        } catch (AuthenticationException e) {

            log.warn("Failed login attempt for username: {}", request.getUsername());
            throw new InvalidCredentialsException("Invalid username or password");
        }
    }


    @Override
    public AuthResponse register(EmployeeRequestDTO request) {
        // Check if username already exists
        if (employeeRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + request.getUsername());
        }

        // Check if email already exists
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }

        // Get department if provided
        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));
        }

        // Create new employee
        Employee employee = Employee.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .status(request.getStatus())
                .role(request.getRole())
                .department(department)
                .build();

        employee = employeeRepository.save(employee);
        UserDetails userDetails = new CustomUserDetails(employee);

        // Generate tokens
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        log.info("New employee registered: {}", request.getUsername());

        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .user(mapToEmployeeDTO(employee))
                .build();
    }

    @Override
    public void logout(String token) {
        // In a production environment, you might want to blacklist the token
        // For now, we'll just log the logout
        String username = jwtService.extractUsername(token);
        log.info("User {} logged out", username);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        try {
            String username = jwtService.extractUsername(refreshToken);

            Employee employee = employeeRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found with username: " + username));

            UserDetails userDetails = new CustomUserDetails(employee);

            if (!jwtService.isTokenValid(refreshToken, userDetails)) {
                throw new TokenExpiredException("Refresh token is expired or invalid.");
            }

            String newAccessToken = jwtService.generateToken(userDetails);
            String newRefreshToken = jwtService.generateRefreshToken(userDetails);

            return AuthResponse.builder()
                    .token(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtService.getExpirationTime())
                    .user(mapToEmployeeDTO(employee))
                    .build();

        } catch (TokenExpiredException e) {
            log.warn("Expired refresh token: {}", e.getMessage());
            throw e;

        } catch (Exception e) {
            throw new InvalidCredentialsException("Invalid refresh token.");
        }
    }


    @Override
    public void changePassword(String username, String oldPassword, String newPassword) {
        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with username: " + username));

        if (!passwordEncoder.matches(oldPassword, employee.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        employee.setPassword(passwordEncoder.encode(newPassword));
        employeeRepository.save(employee);

        log.info("Password changed for user: {}", username);
    }

    @Override
    public void resetPassword(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with email: " + email));

        // In a real application, I would send an email with a reset link
        // For now, we'll just log the action
        log.info("Password reset requested for email: {}", email);

        // TODO: Implement email service for password reset
        throw new UnsupportedOperationException("Password reset functionality not yet implemented");
    }

    private EmployeeResponseDTO mapToEmployeeDTO(Employee employee) {
        DepartmentResponseDTO departmentDTO = null;
        if (employee.getDepartment() != null) {
            departmentDTO = DepartmentResponseDTO.builder()
                    .id(employee.getDepartment().getId())
                    .name(employee.getDepartment().getName())
                    .code(employee.getDepartment().getCode())
                    .build();
        }

        assert departmentDTO != null;
        return EmployeeResponseDTO.builder()
                .id(employee.getId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .username(employee.getUsername())
                .email(employee.getEmail())
                .role(employee.getRole())
                .phoneNumber(employee.getPhoneNumber())
                .status(employee.getStatus())
                .departmentId(departmentDTO.getId())
                .build();
    }
}

