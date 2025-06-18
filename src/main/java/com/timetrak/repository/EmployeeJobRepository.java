package com.timetrak.repository;

import com.timetrak.entity.EmployeeJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeJobRepository extends JpaRepository<EmployeeJob, Long> {
}
