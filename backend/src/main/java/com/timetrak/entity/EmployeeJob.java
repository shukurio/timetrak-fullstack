package com.timetrak.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeJob extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "job_id")
    private Job job;

    private BigDecimal hourlyWage; //

    @OneToMany(mappedBy = "employeeJob", cascade = CascadeType.ALL)
    private List<Shift> shifts;

}

