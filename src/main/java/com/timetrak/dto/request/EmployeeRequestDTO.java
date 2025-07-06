package com.timetrak.dto.request;

import com.timetrak.enums.Role;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeRequestDTO {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
            message = "Username can only contain letters, numbers, dots, underscores, and hyphens")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @NotNull(message = "Role is required")
    private Role role;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$",
            message = "Phone number must be in valid international format (e.g., +1234567890)")
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
    private String password;

    @NotNull(message = "Company ID is required")
    @Positive(message = "Company ID must be a positive number")
    private Long companyId;

    @NotNull(message = "Department ID is required")
    @Positive(message = "Department ID must be a positive number")
    private Long departmentId;

    // ========== HELPER METHODS ==========

    /**
     * Get formatted full name
     */
    public String getFullName() {
        if (firstName == null || lastName == null) {
            return null;
        }
        return firstName.trim() + " " + lastName.trim();
    }

    /**
     * Normalize email to lowercase
     */
    public void normalizeEmail() {
        if (email != null) {
            this.email = email.toLowerCase().trim();
        }
    }

    /**
     * Normalize username to lowercase
     */
    public void normalizeUsername() {
        if (username != null) {
            this.username = username.toLowerCase().trim();
        }
    }

    /**
     * Clean phone number (remove spaces, dashes)
     */
    public void normalizePhoneNumber() {
        if (phoneNumber != null) {
            this.phoneNumber = phoneNumber.replaceAll("[\\s-()]", "");
        }
    }

    /**
     * Apply all normalizations
     */
    public void normalize() {
        normalizeEmail();
        normalizeUsername();
        normalizePhoneNumber();

        // Trim all string fields
        if (firstName != null) firstName = firstName.trim();
        if (lastName != null) lastName = lastName.trim();
    }
}