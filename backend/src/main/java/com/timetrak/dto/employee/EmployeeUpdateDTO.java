package com.timetrak.dto.employee;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeUpdateDTO {
    private String firstName;
    private String lastName;

    @Email(message = "Please provide a valid email address")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$",
            message = "Phone number must be in valid international format (e.g., +1234567890)")
    private String phoneNumber;

}