package com.timetrak.service.payment;

import com.timetrak.dto.payment.JobDetailsDTO;
import com.timetrak.dto.payment.PaymentDetailsDTO;
import com.timetrak.dto.response.ShiftResponseDTO;
import com.timetrak.enums.JobTitle;
import com.timetrak.service.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JobDetailsBuilder {
    private final ShiftService shiftService;

    public List<JobDetailsDTO> buildJobDetailsForPayment(PaymentDetailsDTO payment) {
        // Get shifts for this payment period
        List<ShiftResponseDTO> shifts = shiftService.getShiftsByEmployeeIdAndDateRange(
                payment.getEmployeeId(),
                payment.getPeriodStart(),
                payment.getPeriodEnd()
        );

        // Group shifts by job title and calculate totals
        Map<JobTitle, List<ShiftResponseDTO>> shiftsByJob = shifts.stream()
                .filter(shift -> shift.getJobTitle() != null)
                .collect(Collectors.groupingBy(ShiftResponseDTO::getJobTitle));

        List<JobDetailsDTO> jobDetails = new ArrayList<>();

        for (Map.Entry<JobTitle, List<ShiftResponseDTO>> entry : shiftsByJob.entrySet()) {
            JobTitle jobTitle = entry.getKey();
            List<ShiftResponseDTO> jobShifts = entry.getValue();

            // Calculate totals for this job
            BigDecimal totalHours = jobShifts.stream()
                    .map(ShiftResponseDTO::getTotalHours)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalEarnings = jobShifts.stream()
                    .map(ShiftResponseDTO::getShiftEarnings)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Get hourly rate (assuming same rate for all shifts of same job)
            BigDecimal hourlyRate = jobShifts.stream()
                    .map(ShiftResponseDTO::getHourlyWage)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(BigDecimal.ZERO);

            // Calculate percentages
            BigDecimal percentageOfTotalHours = calculatePercentage(totalHours, payment.getTotalHours());
            BigDecimal percentageOfTotalPay = calculatePercentage(totalEarnings, payment.getTotalEarnings());

            JobDetailsDTO jobDetail = JobDetailsDTO.builder()
                    .jobTitle(jobTitle.name())
                    .totalHours(totalHours)
                    .hourlyRate(hourlyRate)
                    .totalEarnings(totalEarnings)
                    .shiftsCount(jobShifts.size())
                    .percentageOfTotalHours(percentageOfTotalHours)
                    .percentageOfTotalPay(percentageOfTotalPay)
                    .build();

            jobDetails.add(jobDetail);
        }

        return jobDetails;
    }

    private BigDecimal calculatePercentage(BigDecimal part, BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return part.multiply(BigDecimal.valueOf(100))
                .divide(total, 2, RoundingMode.HALF_UP);
    }
}
