package com.timetrak.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "job")
public class Job extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @NotBlank(message = "Job Title should not be empty")
    @Size(min = 2, max = 50, message = "Job Title must be between 2 and 50 characters")
    @Column(name = "job_title", nullable = false)
    private String jobTitle;

    @Column(name = "hourly_wage")
    private BigDecimal hourlyWage;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL)
    private List<EmployeeJob> employeeJobs;

}
