package com.timetrak.repository;

import com.timetrak.dto.response.EmployeeJobInfoDTO;
import com.timetrak.entity.EmployeeJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeJobRepository extends JpaRepository<EmployeeJob, Long> {

    /**
     * Efficient batch query to get all employee and job information needed for group operations.
     * Avoids N+1 query problem by fetching everything in a single JOIN query.
     * 
     * @param employeeJobIds List of EmployeeJob IDs to fetch
     * @return List of EmployeeJobInfoDTO with all necessary information
     */
    @Query("SELECT new com.timetrak.dto.response.EmployeeJobInfoDTO(" +
           "ej.id, " +
           "e.id, " +
           "e.username, " +
           "e.firstName, " +
           "e.lastName, " +
           "j.jobTitle, " +
           "ej.hourlyWage, " +
           "e.company.id) " +
           "FROM EmployeeJob ej " +
           "JOIN ej.employee e " +
           "JOIN ej.job j " +
           "WHERE ej.id IN :employeeJobIds " +
           "AND ej.deletedAt IS NULL " +
           "AND e.deletedAt IS NULL")
    Optional<List<EmployeeJobInfoDTO>> findByIdsWithEmployeeInfo(@Param("employeeJobIds") List<Long> employeeJobIds);

}
