package com.timetrak.dto.clock;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeClockRequestDTO {

    @NotNull(message = "Employee ID cannot be null")
    private Long id;

}
