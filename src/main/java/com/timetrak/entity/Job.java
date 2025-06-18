package com.timetrak.entity;


import com.timetrak.enums.JobTitle;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Job extends BaseEntity{

    @NotBlank(message = "Job Title should not be empty")
    @Size(min = 2, max = 50, message = "Job Title must be between 2 and 50 characters")
    @Column(name="job_title")
    @Enumerated(EnumType.STRING)
    private JobTitle jobTitle;

    @Column(name="hourly_wage")
    private double hourlyWage;
}
