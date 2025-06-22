package com.timetrak.dto.response;

import com.timetrak.enums.EmployeeStatus;
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
    private EmployeeStatus status;
    private Role role;
    private Long companyId;
    private Long departmentId;


}
