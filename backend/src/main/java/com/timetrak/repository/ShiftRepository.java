package com.timetrak.repository;

import com.timetrak.entity.Shift;
import com.timetrak.enums.ShiftStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {

    @Query("SELECT s FROM Shift s " +
            "WHERE s.employee.id = :employeeId " +
            "AND s.deletedAt IS NULL " +
            "AND s.companyId = :companyId")
    Page<Shift> findByEmployeeId(@Param("employeeId") Long employeeId,
                                 @Param("companyId") Long companyId,
                                 Pageable pageable);

    @Query("SELECT s FROM Shift s " +
            "WHERE s.clockIn >= :startDateTime " +
            "AND s.companyId = :companyId " +
            "AND s.deletedAt IS NULL " +
            "ORDER BY s.clockIn DESC")
    Page<Shift> findByDateFrom(@Param("startDateTime") LocalDateTime startDateTime, @Param("companyId") Long companyId, Pageable pageable);

    @Query("SELECT s FROM Shift s " +
            "WHERE s.companyId =:companyId " +
            "AND s.clockIn >= :startDateTime " +
            "AND s.deletedAt IS NULL " +
            "AND s.clockIn <= :endDateTime")
    Page<Shift> findByCompanyIdAndDateRange(@Param ("companyId") Long companyId,
                                            @Param("startDateTime") LocalDateTime startDateTime,
                                            @Param("endDateTime") LocalDateTime endDateTime,
                                            Pageable pageable);

    @Query("SELECT s FROM Shift s " +
            "WHERE s.companyId =:companyId " +
            "AND s.clockIn >= :startDateTime " +
            "AND s.deletedAt IS NULL " +
            "AND s.clockIn <= :endDateTime")
    List<Shift> findByCompanyIdAndDateRange(@Param ("companyId") Long companyId,
                                            @Param("startDateTime") LocalDateTime startDateTime,
                                            @Param("endDateTime") LocalDateTime endDateTime);
    
    @Query("SELECT s FROM Shift s " +
            "WHERE s.employee.id = :employeeId " +
            "AND s.clockIn >= :startDateTime " +
            "AND s.deletedAt IS NULL " +
            "AND s.clockIn <= :endDateTime")
    Page<Shift> findByEmployeeIdAndDateRange(@Param("employeeId") Long employeeId,
                                            @Param("startDateTime") LocalDateTime startDateTime,
                                            @Param("endDateTime") LocalDateTime endDateTime,
                                            Pageable pageable);

    List<Shift> findByStatusAndEmployeeIdAndCompanyIdAndClockInAfterAndDeletedAtIsNull(
            ShiftStatus status,
            Long employeeId,
            Long companyId,
            LocalDateTime startDate
    );


    @Query("SELECT s FROM Shift s " +
            "WHERE DATE(s.clockIn) BETWEEN :startDate AND :endDate " +
            "AND s.companyId = :companyId " +
            "AND s.status = :status " +
            "AND s.deletedAt IS NULL " +
            "ORDER BY s.employee.id, s.clockIn")
    Page<Shift> findAllByStatusAndDateRangeAndCompanyId(
            @Param("status") ShiftStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("companyId") Long companyId,
            Pageable pageable);




    @Query("SELECT COUNT(s) FROM Shift s WHERE s.status = :status AND s.employee.id = :employeeId")
    long countActiveShiftsByEmployeeId(@Param("status") ShiftStatus status, @Param("employeeId") Long employeeId);
    //TODO questioning if i need companyScoped findActiveShiftsByEmployeeId



    // Find specific active shift for employee (for clock out)
    @Query("SELECT s FROM Shift s " +
            "WHERE s.status = 'ACTIVE' " +
            "AND s.deletedAt IS NULL " +
            "AND s.employee.id = :employeeId")
    Optional<Shift> findActiveShiftByEmployeeId(@Param("employeeId") Long employeeId);


    @Query("SELECT CASE WHEN EXISTS(SELECT 1 FROM Shift s WHERE s.status = com.timetrak.enums.ShiftStatus.ACTIVE AND s.employee.id = :employeeId) THEN true ELSE false END")
    boolean hasActiveShifts(@Param("employeeId") Long employeeId);


    @Query("SELECT s FROM Shift s" +
            " WHERE s.employee.id = :employeeId " +
            "AND s.deletedAt IS NULL " +
            "AND s.clockIn >= :startDateTime " +
            "AND s.deletedAt IS NULL " +
            "AND s.clockIn <= :endDateTime")
    List<Shift> findByEmployeeIdAndDateRange(@Param("employeeId") Long employeeId,
                                             @Param("startDateTime") LocalDateTime startDate,
                                             @Param("endDateTime") LocalDateTime endDate);

    @Query("SELECT s FROM Shift s " +
            "WHERE DATE(s.clockIn) BETWEEN :startDate AND :endDate " +
            "AND s.companyId = :companyId " +
            "AND s.clockOut IS NOT NULL " +
            "AND s.status = 'COMPLETED' " +
            "AND s.employee IS NOT NULL " +
            "AND s.deletedAt IS NULL " +
            "ORDER BY s.employee.id, s.clockIn")
    List<Shift> findAllByCompanyIdAndDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("companyId") Long companyId);

    @Query("SELECT s FROM Shift s " +
            "WHERE DATE(s.clockIn) BETWEEN :startDate AND :endDate " +
            "AND s.companyId = :companyId " +
            "AND s.clockOut IS NOT NULL " +
            "AND s.status = 'COMPLETED' " +
            "AND s.employee IS NOT NULL " +
            "AND s.deletedAt IS NULL " +
            "ORDER BY s.employee.id, s.clockIn")
    Page<Shift> findAllByCompanyIdAndDateRangePageable(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("companyId") Long companyId,
            Pageable pageable);

    @Query("SELECT s FROM Shift s " +
            "JOIN s.employee e " +
            "WHERE e.department.id = :departmentId " +
            "AND s.companyId =:companyId " +
            "AND s.deletedAt IS NULL")
    Page<Shift> findByDepartmentIdAndCompanyId(
            @Param("departmentId") Long departmentId,
            @Param("companyId")Long companyId, Pageable pageable);

    @Query("SELECT s FROM Shift s " +
            "WHERE s.employeeJob.job.jobTitle = :jobTitle " +
            "AND s.companyId = :companyId " +
            "AND s.deletedAt IS NULL")
    Page<Shift> findByJobTitle(@Param("jobTitle") String jobTitle,
                               @Param("companyId") Long companyId,
                               Pageable pageable);


    @Query("SELECT s FROM Shift s " +
            "WHERE s.companyId = :companyId " +
            "AND s.employee.department.id = :departmentId " +
            "AND s.clockIn >= :startDate " +
            "AND s.clockIn <= :endDate " +
            "AND s.deletedAt IS NULL " +
            "ORDER BY s.clockIn ASC")
    List<Shift> findShiftsForDepartmentDateRange(
            @Param("departmentId") Long departmentId,
            @Param("companyId") Long companyId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COUNT(s) FROM Shift s "+
    "WHERE s.companyId=:companyId "+
    "AND s.deletedAt IS NULL "+
    "AND s.status=:status "+
     "AND s.clockOut IS NULL")
    long countShiftByStatusInCompany(@Param("companyId") Long companyId,
                                     @Param("status") ShiftStatus status);
}
