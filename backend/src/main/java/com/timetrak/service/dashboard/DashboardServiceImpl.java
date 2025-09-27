package com.timetrak.service.dashboard;

import com.timetrak.dto.company.AdminDashboardDTO;
import com.timetrak.dto.payment.PaymentDetailsDTO;
import com.timetrak.dto.payment.PaymentTotals;
import com.timetrak.dto.payment.Period;
import com.timetrak.dto.shift.ShiftResponseDTO;
import com.timetrak.enums.EmployeeStatus;
import com.timetrak.enums.ShiftStatus;
import com.timetrak.mapper.PaymentMapper;
import com.timetrak.mapper.ShiftMapper;
import com.timetrak.repository.EmployeeRepository;
import com.timetrak.repository.PaymentRepository;
import com.timetrak.repository.ShiftRepository;
import com.timetrak.service.payment.PeriodService;
import com.timetrak.service.payment.calculation.PaymentCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {
    private final EmployeeRepository employeeRepo;
    private final ShiftRepository shiftRepo;
    private final PaymentRepository paymentRepo;
    private final PeriodService periodService;
    private final ShiftMapper shiftMapper;
    private final PaymentMapper paymentMapper;
    private final PaymentCalculator paymentCalculator;

    @Override
    public AdminDashboardDTO getAdminDashboardInfo(Long companyId) {
        Period currentPeriod = periodService.getCurrentPeriod(companyId);
        Period previosPeriod = periodService.getPeriodByNumber(currentPeriod.getPeriodNumber()-1, companyId);

        List<ShiftResponseDTO> currentPeriodShifts = getShiftsForPeriod(companyId, currentPeriod);
        List<ShiftResponseDTO> previousPeriodShifts = getShiftsForPeriod(companyId, previosPeriod);

        PaymentTotals currentPeriodTotals = getShiftTotals(currentPeriodShifts);
        PaymentTotals previousPeriodTotals = getShiftTotals(previousPeriodShifts);

        PaymentTotals popAmountChange = getPopAmountChange(currentPeriodTotals, previousPeriodTotals);
        PaymentTotals popChange = getPopChange(currentPeriodTotals, previousPeriodTotals);

        List<PaymentDetailsDTO> recentPayments = getRecentPayments(companyId,currentPeriod);



        return AdminDashboardDTO.builder()
                .activeEmployeeCount(getEmployeeCountByStatus(companyId,EmployeeStatus.ACTIVE))
                .pendingEmployeeCount(getEmployeeCountByStatus(companyId,EmployeeStatus.PENDING))
                .activeShiftsCount(getActiveShiftCount(companyId))
                .thisPeriodHours(currentPeriodTotals.getTotalHours())
                .thisPeriodRevenue(currentPeriodTotals.getTotalEarnings())
                .popHoursAmountChange(popAmountChange.getTotalHours())
                .popRevenueAmountChange(popAmountChange.getTotalEarnings())
                .popHoursChange(popChange.getTotalHours())
                .popRevenueChange(popChange.getTotalEarnings().doubleValue())
                .recentPayments(recentPayments.stream().limit(5).collect(Collectors.toList()))
                .recentShifts(currentPeriodShifts.stream().limit(5).collect(Collectors.toList()))
                .build();

    }

    private long getActiveShiftCount(Long companyId){
        return shiftRepo.countShiftByStatusInCompany(companyId, ShiftStatus.ACTIVE);
    }

    private long getEmployeeCountByStatus(Long companyId, EmployeeStatus status) {
        return employeeRepo.countEmployeesByStatusInCompany(companyId, status);
    }

    private LocalDateTime startOfDay(LocalDate date) {
        return date.atStartOfDay();
    }
    private LocalDateTime endOfDay(LocalDate date) {
        return date.atTime(23, 59, 59, 999999999);
    }

    private List<ShiftResponseDTO> getShiftsForPeriod(Long companyId, Period period) {
               return  shiftRepo.findByCompanyIdAndDateRange(
                                companyId,
                                startOfDay(period.getStartDate()),
                                endOfDay(period.getEndDate())).stream()
                        .map(shiftMapper::toDTO).toList();
    }

    private List<PaymentDetailsDTO> getRecentPayments(Long companyId, Period period) {
        return paymentRepo.findByCompanyIdAndDateRange(companyId,period.getStartDate(),period.getEndDate())
                .stream().map(paymentMapper::toDTO).toList();
    }

    private PaymentTotals getShiftTotals(List<ShiftResponseDTO> shifts) {
        return paymentCalculator.calculateShiftTotals(shifts);

    }

    private PaymentTotals getPopAmountChange(PaymentTotals current, PaymentTotals previous) {
        Double hoursChange = current.getTotalHours()-previous.getTotalHours();
        BigDecimal revenueChange = current.getTotalEarnings().subtract(previous.getTotalEarnings()).setScale(2, RoundingMode.HALF_UP);
        return PaymentTotals.builder()
                .totalHours(hoursChange)
                .totalEarnings(revenueChange)
                .build();
    }

    private PaymentTotals getPopChange(PaymentTotals current, PaymentTotals previous) {

        double hoursChange = 0.00;
        if(previous.getTotalHours()!=null && previous.getTotalHours()>0){
            hoursChange = Math.round(((current.getTotalHours() /
                    previous.getTotalHours()) * 100) * 100.0) /
                    100.0;
        }


        BigDecimal revenueChange = BigDecimal.ZERO;
        if(previous.getTotalEarnings()!=null && previous.getTotalEarnings().doubleValue()>0 ){
             revenueChange = (current.getTotalEarnings().divide(previous.getTotalEarnings(), 2, RoundingMode.HALF_UP)).multiply(new BigDecimal(100));
        }

        return PaymentTotals.builder()
                .totalHours(hoursChange)
                .totalEarnings(revenueChange)
                .build();
    }




}
