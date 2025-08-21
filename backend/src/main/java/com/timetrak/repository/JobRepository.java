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

    Optional<Job> findByIdAndCompanyIdAndDeletedAtIsNull(Long jobId, Long companyId);
     boolean existsByJobTitleAndCompanyIdAndDeletedAtIsNull(String jobTitle, Long companyId);
    List<Job> findByCompanyIdAndDeletedAtIsNull(Long companyId);
    Page<Job> findByCompanyIdAndDeletedAtIsNull(Long companyId, Pageable pageable);
    @Query("SELECT j FROM Job j WHERE j.company.id = :companyId AND j.deletedAt IS NULL " +
            "AND (LOWER(j.jobTitle) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Job> searchByJobTitle(@Param("query") String query, @Param("companyId") Long companyId);
}
