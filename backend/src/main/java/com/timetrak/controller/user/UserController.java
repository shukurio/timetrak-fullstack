package com.timetrak.controller.user;

import com.timetrak.dto.employee.EmployeeResponseDTO;
import com.timetrak.dto.employee.EmployeeUpdateDTO;
import com.timetrak.service.auth.AuthContextService;
import com.timetrak.service.employee.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {
    private final EmployeeService employeeService;
    private final AuthContextService authContextService;

    @PutMapping("/profile")
    public ResponseEntity<EmployeeResponseDTO> updateUserInfo(@Valid @RequestBody EmployeeUpdateDTO request) {

        EmployeeResponseDTO response = employeeService.updateEmployee(currentEmployeeId(),request, currentCompanyId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<EmployeeResponseDTO> getUserInfo() {

        EmployeeResponseDTO user = employeeService.getEmployeeDTOById(currentEmployeeId());
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Change password", description = "Change user password")
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestBody Map<String, String> request) {

        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        if (oldPassword == null || newPassword == null ||
                oldPassword.isEmpty() || newPassword.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Old password and new password are required"));
        }

        employeeService.changePassword(authContextService.getCurrentUsername(), oldPassword, newPassword);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    private Long currentEmployeeId() {
        return authContextService.getCurrentEmployeeId();
    }
    private Long currentCompanyId(){
        return authContextService.getCurrentCompanyId();
    }



}
