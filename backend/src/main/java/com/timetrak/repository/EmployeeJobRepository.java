package com.timetrak.repository;

import com.timetrak.entity.EmployeeJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeJobRepository extends JpaRepository<EmployeeJob, Long> {

    @Query("SELECT ej FROM EmployeeJob ej " +
            "WHERE ej.id IN :employeeJobIds " +
            "AND ej.deletedAt IS NULL ")
    List<EmployeeJob> findByIdsWithEmployeeInfo(@Param("employeeJobIds") List<Long> employeeJobIds);

    @Query("SELECT ej FROM EmployeeJob ej WHERE ej.employee.company.id = :companyId AND ej.deletedAt IS NULL")
    List<EmployeeJob> findByCompanyIdAndDeletedAtIsNull(@Param("companyId") Long companyId);

    @Query("SELECT ej FROM EmployeeJob ej WHERE ej.employee.id = :employeeId AND ej.employee.company.id = :companyId AND ej.deletedAt IS NULL")
    List<EmployeeJob> findByEmployeeIdAndCompanyId(@Param("employeeId") Long employeeId, @Param("companyId") Long companyId);

    @Query("SELECT ej FROM EmployeeJob ej WHERE ej.job.id = :jobId AND ej.employee.company.id = :companyId AND ej.deletedAt IS NULL")
    List<EmployeeJob> findByJobIdAndCompanyId(@Param("jobId") Long jobId, @Param("companyId") Long companyId);

    @Query("SELECT ej FROM EmployeeJob ej WHERE ej.job.department.id = :departmentId AND ej.employee.company.id = :companyId AND ej.deletedAt IS NULL")
    List<EmployeeJob> findByJobDepartmentIdAndCompanyId(@Param("departmentId") Long departmentId, @Param("companyId") Long companyId);

    @Query("SELECT ej FROM EmployeeJob ej WHERE ej.id = :employeeJobId AND ej.employee.company.id = :companyId AND ej.deletedAt IS NULL")
    Optional<EmployeeJob> findByIdAndCompanyId(@Param("employeeJobId") Long employeeJobId, @Param("companyId") Long companyId);

    @Query("SELECT CASE WHEN COUNT(ej) > 0 THEN true ELSE false END FROM EmployeeJob ej WHERE ej.employee.id = :employeeId AND ej.job.id = :jobId AND ej.employee.company.id = :companyId AND ej.deletedAt IS NULL")
    boolean existsByEmployeeIdAndJobIdAndCompanyId(@Param("employeeId") Long employeeId, @Param("jobId") Long jobId, @Param("companyId") Long companyId);

    @Query("SELECT ej FROM EmployeeJob ej JOIN ej.employee e WHERE e.username = :username AND e.company.id = :companyId AND ej.deletedAt IS NULL")
    List<EmployeeJob> findByEmployeeUsernameAndCompanyId(@Param("username") String username, @Param("companyId") Long companyId);

    Optional<EmployeeJob> findByIdAndDeletedAtIsNull(Long empJobId);
}
