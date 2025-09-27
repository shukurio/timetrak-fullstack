package com.timetrak.repository;

import com.timetrak.entity.Employee;
import com.timetrak.enums.EmployeeStatus;
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

    @Query("SELECT e FROM Employee e WHERE e.id = :employeeId AND e.company.id = :companyId AND e.status = 'ACTIVE' AND e.deletedAt IS NULL")
    Optional<Employee> findActiveByIdAndCompanyId(@Param("employeeId") Long employeeId, @Param("companyId") Long companyId);
    
    // Legacy method (any status, not deleted)
    @Query("SELECT e FROM Employee e WHERE e.id = :employeeId AND e.company.id = :companyId AND e.deletedAt IS NULL")
    Optional<Employee> findByIdAndCompanyIdAndDeletedAtIsNull(@Param("employeeId") Long employeeId, @Param("companyId") Long companyId);

    // Basic lookups with company scope
    @Query("SELECT e FROM Employee e WHERE e.username = :username AND e.deletedAt IS NULL AND e.status = 'ACTIVE'")
    Optional<Employee> findActiveByUsername(@Param("username") String username);

    // Legacy methods (keeping for backward compatibility)
    Optional<Employee> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);

    // Legacy method (keeping for backward compatibility)
    @Query("SELECT CONCAT(e.firstName, ' ', e.lastName) FROM Employee e WHERE e.id = :employeeId AND e.deletedAt IS NULL")
    Optional<String> findFullNameById(@Param("employeeId") Long employeeId);


    // Department-based
    @Query("SELECT e FROM Employee e WHERE e.company.id =:companyId AND e.department.id = :departmentId AND e.deletedAt IS NULL")
    Page<Employee> findByDepartmentIdActive(@Param("departmentId") Long departmentId,
                                            @Param("companyId") Long companyId,
                                            Pageable pageable);


    @Query("SELECT e FROM Employee e WHERE e.deletedAt IS NULL AND e.company.id = :companyId AND e.status = :status")
    Page<Employee> findAllByCompanyIdAndStatusPaged(
            @Param("companyId") Long companyId,
            @Param("status") EmployeeStatus status,
            Pageable pageable);

    // Full-text search (basic) - paged
    @Query("SELECT e FROM Employee e WHERE e.deletedAt IS NULL AND e.company.id = :companyId AND e.status = 'ACTIVE' AND " +
            "(LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Employee> searchActiveEmployees(@Param("search") String search, @Param("companyId") Long companyId, Pageable pageable);



    @Query("SELECT e.id FROM Employee e WHERE e.company.id = :companyId AND e.status = 'ACTIVE'")
    List<Long> findActiveEmployeeIdsByCompanyId(@Param("companyId") Long companyId);

    Page<Employee> findAllByCompanyIdAndDeletedAtIsNull(Long companyId, Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE e.company.id = :companyId AND e.status='ACTIVE'")
    Page<Employee> findAllActiveByCompanyId(@Param("companyId")Long companyId, Pageable pageable);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.department.id = :departmentId AND e.status = 'ACTIVE' AND e.deletedAt IS NULL")
    long countActiveEmployeesInDepartment(@Param("departmentId") Long departmentId);

    @Query("SELECT COUNT(e) FROM Employee e " +
    "WHERE e.company.id= :companyId "+
    "AND e.deletedAt IS NULL "+
    "AND e.status=:status")
    long countEmployeesByStatusInCompany(@Param("companyId") Long companyId,
                                         @Param("status") EmployeeStatus status);



}
