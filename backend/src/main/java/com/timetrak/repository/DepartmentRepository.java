package com.timetrak.repository;

import com.timetrak.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    //TODO when JWT is implemneted, add another arg so that i can ceck current
    // users companyId and what user is trying to access, i ned them to match

    Optional<Department> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT d FROM Department d WHERE d.deletedAt IS NULL AND d.isActive = true")
    Page<Department> findAllActiveAndEnabled(Pageable pageable);

    @Query("SELECT d FROM Department d WHERE d.company.id = :companyId AND d.deletedAt IS NULL AND d.isActive = true")
    Page<Department> findActiveByCompanyId(Long companyId, Pageable pageable);

    boolean existsByIdAndCompanyId(Long departmentId, Long companyId);

    Optional<Department> findByIdAndCompanyIdAndDeletedAtIsNull(Long id, Long companyId);

    List<Department> findAllByCompanyIdAndIsActiveTrue(Long companyId);
}
