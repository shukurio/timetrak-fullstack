package com.timetrak.service.payment;

import com.timetrak.dto.payment.JobDetailsDTO;
import com.timetrak.dto.payment.PaymentDetailsDTO;
import com.timetrak.dto.response.ShiftResponseDTO;
import com.timetrak.service.shift.ShiftService;
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
        Map<String, List<ShiftResponseDTO>> shiftsByJob = shifts.stream()
                .filter(shift -> shift.getJobTitle() != null)
                .collect(Collectors.groupingBy(ShiftResponseDTO::getJobTitle));

        List<JobDetailsDTO> jobDetails = new ArrayList<>();

        for (Map.Entry<String, List<ShiftResponseDTO>> entry : shiftsByJob.entrySet()) {
            String jobTitle = entry.getKey();
            List<ShiftResponseDTO> jobShifts = entry.getValue();

            // Calculate totals for this job
            Double totalHours = jobShifts.stream()
                    .map(ShiftResponseDTO::getHours)
                    .filter(Objects::nonNull)
                    .reduce(0.0, Double::sum);

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
            Double percentageOfTotalHours = calculateHoursPercentage(totalHours, payment.getTotalHours());
            Double percentageOfTotalPay = calculateEarningsPercentage(totalEarnings, payment.getTotalEarnings());

            JobDetailsDTO jobDetail = JobDetailsDTO.builder()
                    .jobTitle(jobTitle)
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

    private Double calculateHoursPercentage(Double part, Double total) {
        if (total == null || total == 0.0) {
            return 0.0;
        }
        return (part / total) * 100.0;
    }

    private Double calculateEarningsPercentage(BigDecimal part, BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return part.divide(total, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }
}
