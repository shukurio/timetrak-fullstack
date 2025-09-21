package com.timetrak.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.timetrak.enums.ShiftStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Table(name="shift")
public class Shift extends BaseEntity{

    @ManyToOne
    @JoinColumn(name="employee_job_id")
    private EmployeeJob employeeJob;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer"})
    private Employee employee;


    @Column(name = "company_id", nullable = false)
    private Long companyId;


    @Column(name="clock_in")
    private LocalDateTime clockIn;

    @Column(name="clock_out")
    private LocalDateTime clockOut;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name="status")
    private ShiftStatus status;


    public void setEmployeeJob(EmployeeJob employeeJob) {
        this.employeeJob = employeeJob;
        this.employee = employeeJob.getEmployee(); // Auto-sync
    }

}