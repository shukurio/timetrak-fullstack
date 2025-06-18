package com.timetrak.dto;

import com.timetrak.enums.Role;
import lombok.*;


@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EmployeeResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String phoneNumber;
    private Boolean isActive;
    private Role role;
    private Long companyId;
    private Long departmentId;


}
