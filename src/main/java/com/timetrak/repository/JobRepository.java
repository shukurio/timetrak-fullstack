package com.timetrak.repository;

import com.timetrak.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    @Query("SELECT j FROM Job j WHERE j.department.company.id = :companyId AND j.deletedAt IS NULL")
    List<Job> findByCompanyIdAndDeletedAtIsNull(@Param("companyId") Long companyId);

    @Query("SELECT j FROM Job j WHERE j.department.company.id = :companyId AND j.deletedAt IS NULL")
    Page<Job> findByCompanyIdAndDeletedAtIsNull(@Param("companyId") Long companyId, Pageable pageable);

    @Query("SELECT j FROM Job j WHERE j.id = :jobId AND j.department.company.id = :companyId AND j.deletedAt IS NULL")
    Optional<Job> findByIdAndCompanyIdAndDeletedAtIsNull(@Param("jobId") Long jobId, @Param("companyId") Long companyId);

    @Query("SELECT j FROM Job j WHERE j.department.id = :departmentId AND j.department.company.id = :companyId AND j.deletedAt IS NULL")
    List<Job> findByDepartmentIdAndCompanyIdAndDeletedAtIsNull(@Param("departmentId") Long departmentId, @Param("companyId") Long companyId);

    @Query("SELECT j FROM Job j WHERE j.department.id = :departmentId AND j.department.company.id = :companyId AND j.deletedAt IS NULL")
    Page<Job> findByDepartmentIdAndCompanyIdAndDeletedAtIsNull(@Param("departmentId") Long departmentId, @Param("companyId") Long companyId, Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(j) > 0 THEN true ELSE false END FROM Job j WHERE j.jobTitle = :jobTitle AND j.department.id = :departmentId AND j.department.company.id = :companyId AND j.deletedAt IS NULL")
    boolean existsByJobTitleAndDepartmentIdAndCompanyIdAndDeletedAtIsNull(@Param("jobTitle") String jobTitle, @Param("departmentId") Long departmentId, @Param("companyId") Long companyId);

    @Query("SELECT j FROM Job j WHERE j.department.company.id = :companyId AND j.deletedAt IS NULL AND LOWER(j.jobTitle) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Job> searchByJobTitle(@Param("query") String query, @Param("companyId") Long companyId);
}