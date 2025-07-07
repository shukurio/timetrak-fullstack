package com.timetrak.repository;

import com.timetrak.entity.Employee;
import com.timetrak.enums.EmployeeStatus;
import com.timetrak.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Basic lookups
    Optional<Employee> findByUsername(String username);
    Optional<Employee> findByEmail(String email);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);


    @Query("SELECT CONCAT(e.firstName, ' ', e.lastName) FROM Employee e WHERE e.id = :employeeId AND e.deletedAt IS NULL")
    Optional<String> findFullNameById(@Param("employeeId") Long employeeId);

    // Role-based
    List<Employee> findByRole(Role role);

    // Status-based
    List<Employee> findByStatus(EmployeeStatus status);
    List<Employee> findByStatusAndDeletedAtIsNull(EmployeeStatus status);

    // Department-based
    @Query("SELECT e FROM Employee e WHERE e.department.id = :departmentId AND e.deletedAt IS NULL")
    Page<Employee> findByDepartmentIdActive(@Param("departmentId") Long departmentId, Pageable pageable);


    @Query("SELECT e FROM Employee e WHERE e.deletedAt IS NULL")
    Page<Employee> findAllActive(Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE e.deletedAt IS NULL AND e.status = :status")
    Page<Employee> findByStatusPaged(@Param("status") EmployeeStatus status, Pageable pageable);

    // Full-text search (basic) - paged
    @Query("SELECT e FROM Employee e WHERE e.deletedAt IS NULL AND " +
            "(LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Employee> searchActiveEmployees(@Param("search") String search, Pageable pageable);



}
