package com.timetrak.repository;

import com.timetrak.entity.Payment;
import com.timetrak.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // Core business queries only
    Page<Payment> findByEmployeeIdOrderByPeriodEndDesc(Long employeeId, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.employee.company.id = :companyId ORDER BY p.periodEnd DESC")
    Page<Payment> findByCompanyId(@Param("companyId") Long companyId, Pageable pageable);

    List<Payment> findByStatusOrderByCalculatedAtDesc(PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.employee.id = :employeeId " +
            "AND p.periodStart = :periodStart AND p.periodEnd = :periodEnd")
    Optional<Payment> findByEmployeeAndPeriod(@Param("employeeId") Long employeeId,
                                              @Param("periodStart") LocalDate periodStart,
                                              @Param("periodEnd") LocalDate periodEnd);

    @Query("SELECT COUNT(p) > 0 FROM Payment p " +
            "WHERE p.employee.id = :employeeId " +
            "AND p.periodStart = :startDate " +
            "AND p.periodEnd = :endDate " +
            "AND p.companyId = :companyId " +
            "AND p.status != :status")
    boolean existsByEmployeeIdAndDateRangeAndCompanyIdAndStatusNot(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("companyId") Long companyId,
            @Param("status") PaymentStatus status);

    boolean existsByIdAndCompanyId(Long paymentId, Long CompanyId);

    boolean existsByIdAndEmployeeId(Long paymentId, Long employeeId);

    boolean existsByCompanyIdAndPeriodStartAndPeriodEnd(Long companyId, LocalDate periodStart, LocalDate periodEnd);

    @Query("SELECT DISTINCT p.employee.id FROM Payment p " +
            "WHERE p.employee.id IN :employeeIds " +
            "AND p.periodStart = :periodStart " +
            "AND p.periodEnd = :periodEnd " +
            "AND p.status != :excludedStatus")
    List<Long> findEmployeesWithExistingPaymentsInPeriod(
            @Param("employeeIds") List<Long> employeeIds,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd,
            @Param("excludedStatus") PaymentStatus excludedStatus);

    // ✅ In PaymentRepository
    @Query("SELECT DISTINCT p.employee.id FROM Payment p " +
            "WHERE p.employee.id IN :employeeIds " +
            "AND p.periodStart = :startDate " +
            "AND p.periodEnd = :endDate " +
            "AND p.companyId = :companyId " +
            "AND p.status != :excludeStatus")
    List<Long> findEmployeeIdsWithExistingPayments(
            @Param("employeeIds") List<Long> employeeIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("companyId") Long companyId,
            @Param("excludeStatus") PaymentStatus excludeStatus);}
