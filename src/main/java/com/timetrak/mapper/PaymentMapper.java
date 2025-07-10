package com.timetrak.mapper;

import com.timetrak.dto.response.JobDetailDTO;
import com.timetrak.dto.response.JobDetailsDTO;
import com.timetrak.dto.response.PaymentResponseDTO;
import com.timetrak.entity.Payment;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    /**
     * Maps Payment entity to PaymentResponseDTO with job details
     */
    @Mapping(target = "employeeId", source = "payment.employee.id")
    @Mapping(target = "employeeName", source = "payment.employee.fullName")
    @Mapping(target = "employeeUsername", source = "payment.employee.username")
    @Mapping(target = "jobDetails", source = "jobDetails")
    @Mapping(target = "jobsCount", expression = "java(getJobsCount(jobDetails))")
    @Mapping(target = "calculatedByName", ignore = true)
    @Mapping(target = "checkNumber", ignore = true)
    @Mapping(target = "averageHourlyRate", expression = "java(calculateAverageRate(payment))")
    PaymentResponseDTO toResponseDTO(Payment payment, List<JobDetailsDTO> jobDetails);

    /**
     * Simple mapping without job details
     */
    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", source = "employee.fullName")
    @Mapping(target = "employeeUsername", source = "employee.username")
    @Mapping(target = "jobDetails", ignore = true)
    @Mapping(target = "jobsCount", constant = "0")
    @Mapping(target = "calculatedByName", ignore = true)
    @Mapping(target = "checkNumber", ignore = true)
    @Mapping(target = "averageHourlyRate", expression = "java(calculateAverageRate(payment))")
    PaymentResponseDTO toSimpleResponseDTO(Payment payment);

    /**
     * Helper method to get jobs count
     */
    default Integer getJobsCount(List<JobDetailsDTO> jobDetails) {
        return jobDetails != null ? jobDetails.size() : 0;
    }

    /**
     * Calculate average hourly rate
     */
    default BigDecimal calculateAverageRate(Payment payment) {
        if (payment.getTotalHours() == null || payment.getTotalAmount() == null) {
            return BigDecimal.ZERO;
        }

        if (payment.getTotalHours().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return payment.getTotalAmount()
                .divide(payment.getTotalHours(), 2, RoundingMode.HALF_UP);
    }
}