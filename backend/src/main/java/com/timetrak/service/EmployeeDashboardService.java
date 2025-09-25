package com.timetrak.service;

import com.timetrak.dto.payment.Period;
import com.timetrak.dto.employee.EmployeeDashboardSummary;
import com.timetrak.dto.shift.ShiftResponseDTO;
import com.timetrak.entity.Employee;
import com.timetrak.entity.Payment;
import com.timetrak.entity.Shift;
import com.timetrak.enums.ShiftStatus;
import com.timetrak.exception.ResourceNotFoundException;
import com.timetrak.service.employee.EmployeeService;
import com.timetrak.service.payment.PeriodService;
import com.timetrak.service.payment.calculation.PaymentCalculator;
import com.timetrak.service.shift.ShiftService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EmployeeDashboardService {

    private final ShiftService shiftService;
    private final PeriodService periodService;
    private final EmployeeService employeeService;
    private final PaymentCalculator paymentCalculator;

    public EmployeeDashboardSummary getDashboardSummary(Long employeeId, Long companyId) {

        //====Active Shift metrics====///
        Shift activeShift;

        try{
            activeShift = shiftService.getActiveShiftSelf(employeeId);
        }catch(ResourceNotFoundException e){
            activeShift = null;
        }
        BigDecimal hoursToday = BigDecimal.ZERO;
        BigDecimal earningsToday=BigDecimal.ZERO;

        if (activeShift != null && activeShift.getClockIn() != null) {
            LocalDateTime clockIn = activeShift.getClockIn();
            LocalDateTime now = LocalDateTime.now();
            hoursToday = BigDecimal.valueOf(
                    Duration.between(clockIn, now).toMinutes()
            ).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        }

        if(hoursToday.compareTo(BigDecimal.ZERO) > 0 && activeShift!=null) {
            earningsToday = getHourlyWage(activeShift).multiply(hoursToday);
        }


        Employee employee = employeeService.getById(employeeId,companyId);
        Period currentPeriod = periodService.getCurrentPeriod(companyId);

        List<ShiftResponseDTO> shifts =
                shiftService.getShiftByStatusAndEmployeeIdAndStartDate(
                        ShiftStatus.COMPLETED,
                        employeeId,
                        companyId,
                        currentPeriod.getStartDate().atStartOfDay());

        // Build the summary
        return EmployeeDashboardSummary.builder()
                // Period info
                .periodStart(currentPeriod.getStartDate())
                .periodEnd(currentPeriod.getEndDate())
                .formattedPeriod(currentPeriod.generateShortDescription())
                .periodNumber(currentPeriod.getPeriodNumber())

                // Current period metrics
                .currentPeriodHours(getCurrentPeriodHours(shifts,hoursToday))
                .currentPeriodEarnings(getCurrentPeriodEarnings(employee,shifts,currentPeriod,earningsToday))
                .currentPeriodShifts((shifts.size()))

                // Today metrics
                .todayHours(hoursToday)
                .todayEarnings(earningsToday)
                .todayShifts(activeShift != null ? 1 : 0)

                // Performance metrics
                .averageHourlyRate(getAverageHourlyRate(employee,shifts, currentPeriod,hoursToday,earningsToday))

                .build();
    }

    // ============= CURRENT PERIOD CALCULATIONS =============

    private BigDecimal getCurrentPeriodHours(List<ShiftResponseDTO> shifts,BigDecimal hoursToday) {

        BigDecimal hours = BigDecimal.ZERO;

        for (ShiftResponseDTO shift : shifts) {
            long durationInMinutes = java.time.Duration.between(
                    shift.getClockIn(),
                    shift.getClockOut()
            ).toMinutes();

            BigDecimal shiftHours = BigDecimal.valueOf(durationInMinutes)
                    .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

            hours = hours.add(shiftHours);
        }

        return hours.add(hoursToday);
    }


    private BigDecimal getCurrentPeriodEarnings(Employee employee,
                                                List<ShiftResponseDTO> shifts,
                                                Period currentPeriod,
                                                BigDecimal earningsToday) {
        Payment payment = paymentCalculator.calculateSingleEmployeePayment(employee, shifts, currentPeriod, 0L);

        BigDecimal totalEarnings = payment != null && payment.getTotalEarnings() != null
                ? payment.getTotalEarnings()
                : BigDecimal.ZERO;

        if (totalEarnings.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return totalEarnings.add(earningsToday);
    }

    private BigDecimal getHourlyWage(Shift activeShift) {
        if (activeShift.getEmployeeJob() != null) {
            BigDecimal empWage = activeShift.getEmployeeJob().getHourlyWage();

            if (empWage != null && empWage.compareTo(BigDecimal.ZERO) > 0) {
                return empWage; // use employee override
            } else {
                return activeShift.getEmployeeJob().getJob().getHourlyWage(); // fallback to job wage
            }
        } else {
            return BigDecimal.ZERO;
        }
    }



    // ============= PERFORMANCE METRICS =============

    private BigDecimal getAverageHourlyRate(Employee employee,
                                            List<ShiftResponseDTO> shifts,
                                            Period period,
                                            BigDecimal hoursToday,
                                            BigDecimal earningsToday) {
        BigDecimal totalHours = getCurrentPeriodHours(shifts,hoursToday);
        BigDecimal totalEarnings = getCurrentPeriodEarnings(employee,shifts, period, earningsToday);

        if (totalHours.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return totalEarnings.divide(totalHours, 2, RoundingMode.HALF_UP);
    }



    // ============= HELPERS =============

}