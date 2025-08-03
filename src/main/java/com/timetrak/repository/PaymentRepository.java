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

    @Query("SELECT p FROM Payment p WHERE p.id = :paymentId " +
            "AND p.employee.id = :employeeId AND p.companyId = :companyId")
    Optional<Payment> findByIdAndEmployeeIdAndCompanyId(
            @Param("paymentId") Long paymentId,
            @Param("employeeId") Long employeeId,
            @Param("companyId") Long companyId);

    Optional<Payment> findByIdAndCompanyId(Long id, Long companyId);

    @Query("SELECT p FROM Payment p WHERE p.employee.company.id = :companyId ORDER BY p.periodEnd DESC")
    Page<Payment> findByCompanyId(@Param("companyId") Long companyId, Pageable pageable);

    boolean existsByCompanyIdAndPeriodStartAndPeriodEnd(Long companyId, LocalDate periodStart, LocalDate periodEnd);

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
            @Param("excludeStatus") PaymentStatus excludeStatus);

    Page<Payment> findByEmployeeIdAndCompanyId(Long employeeId,
                                                         Long companyId,
                                                         Pageable pageable);

    @Query("SELECT p FROM Payment p where p.companyId =:companyId AND p.status =:status")
    Page<Payment> findByCompanyIdAndStatus(
            @Param("companyId")Long companyId,
            @Param("status")PaymentStatus status,
            Pageable pageable);


    @Query("SELECT p FROM Payment p WHERE p.id IN :paymentIds AND p.companyId = :companyId")
    List<Payment> findAllByIdsAndCompanyId(@Param("paymentIds") List<Long> paymentIds,
                                          @Param("companyId") Long companyId);

    @Query("SELECT p FROM Payment p WHERE p.companyId = :companyId AND p.periodStart BETWEEN :startDate AND :endDate")
    List<Payment> findByCompanyIdAndDateRange(@Param("companyId") Long companyId,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);



}
