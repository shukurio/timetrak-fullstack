package com.timetrak.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Shift extends BaseEntity{
    @ManyToOne
    @JoinColumn(name="employee_job_id")
    private EmployeeJob employeeJob;

    @Column(name="clock_in")
    private java.time.LocalDateTime clockIn;

    @Column(name="clock_out")
    private java.time.LocalDateTime clockOut;


}