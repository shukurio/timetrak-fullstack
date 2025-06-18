package com.timetrak.repository;

import com.timetrak.entity.Employee;
import com.timetrak.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByUsername(String username);

    Optional<Employee> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<Employee> findByRole(Role role);

    List<Employee> findByIsActiveTrue();

    List<Employee> findByDepartmentId(Long departmentId);

    @Query("SELECT e FROM Employee e WHERE e.deletedAt IS NULL")
    List<Employee> findAllActive();

    @Query("SELECT e FROM Employee e WHERE e.deletedAt IS NULL")
    Page<Employee> findAllActive(Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE e.deletedAt IS NULL AND e.isActive = true")
    List<Employee> findAllActiveAndEnabled();

    @Query("SELECT e FROM Employee e WHERE e.deletedAt IS NULL AND e.isActive = true")
    Page<Employee> findAllActiveAndEnabled(Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE e.deletedAt IS NULL AND e.isActive = true AND " +
            "(LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Employee> searchActiveEmployees(@Param("search") String search);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.deletedAt IS NULL AND e.isActive = true")
    long countActiveEmployees();

    @Query("SELECT e FROM Employee e WHERE e.createdAt >= :startDate AND e.createdAt <= :endDate")
    List<Employee> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);
}
