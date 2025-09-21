package com.timetrak.service.auth;


import com.timetrak.entity.Employee;
import com.timetrak.enums.EmployeeStatus;
import com.timetrak.exception.InvalidCredentialsException;
import com.timetrak.exception.ResourceNotFoundException;
import com.timetrak.exception.TokenExpiredException;
import com.timetrak.mapper.EmployeeMapper;
import com.timetrak.repository.DepartmentRepository;
import com.timetrak.repository.EmployeeRepository;
import com.timetrak.security.auth.CustomUserDetails;
import com.timetrak.security.auth.JwtService;
import com.timetrak.security.auth.dto.AuthRequest;
import com.timetrak.security.auth.dto.AuthResponse;
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
    private final EmployeeMapper employeeMapper;

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
                //TODO Dont hArdcode
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
                    .user(employeeMapper.toDTO(employee))
                    .build();

        } catch (AuthenticationException e) {

            log.warn("Failed login attempt for username: {}", request.getUsername());
            throw new InvalidCredentialsException("Invalid username or password");
        }
        catch(Exception e){
            log.error("Unexpected error during authentication: {}", e.getMessage(), e);
            throw e;
        }
    }


    @Override
    public void logout(String token) {
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
                    .user(employeeMapper.toDTO(employee))
                    .build();

        } catch (TokenExpiredException e) {
            log.warn("Expired refresh token: {}", e.getMessage());
            throw e;

        } catch (Exception e) {
            throw e;
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
        //TODO Implement  Email system
        log.info("Password reset requested for email: {}", email);

        // TODO: Implement email service for password reset
        throw new UnsupportedOperationException("Password reset functionality not yet implemented");
    }

}

