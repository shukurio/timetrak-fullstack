package com.timetrak.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(name = "employee_job")
public class EmployeeJob extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(name = "hourly_wage")
    private BigDecimal hourlyWage; // Optional override, else fallback to job's wage

    @OneToMany(mappedBy = "employeeJob", cascade = CascadeType.ALL)
    private List<Shift> shifts;

    // Convenience getters
    public String getEmployeeFullName() {
        return employee != null ? employee.getFullName() : null;
    }

    public String getJobTitle() {
        return job != null ? job.getJobTitle() : null;
    }

    public BigDecimal getEffectiveHourlyWage() {
        return hourlyWage != null ? hourlyWage : (job != null ? job.getHourlyWage() : BigDecimal.ZERO);
    }
}
