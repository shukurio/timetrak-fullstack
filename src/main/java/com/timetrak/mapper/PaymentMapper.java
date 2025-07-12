package com.timetrak.mapper;

import com.timetrak.dto.payment.JobDetailsDTO;
import com.timetrak.dto.payment.PaymentResponseDTO;
import com.timetrak.entity.Payment;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "employeeId", source = "payment.employee.id")
    @Mapping(target = "employeeName", source = "payment.employee.fullName")
    @Mapping(target = "employeeUsername", source = "payment.employee.username")
    @Mapping(target = "jobDetails", source = "jobDetails")
    @Mapping(target = "jobsCount", expression = "java(getJobsCount(jobDetails))")
    @Mapping(target = "calculatedByName", ignore = true)
    @Mapping(target = "checkNumber", ignore = true)
    @Mapping(target = "averageHourlyRate", expression = "java(calculateAverageRate(payment))")
    PaymentResponseDTO toResponseDTO(Payment payment, List<JobDetailsDTO> jobDetails);

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", source = "employee.fullName")
    @Mapping(target = "employeeUsername", source = "employee.username")
    @Mapping(target = "jobDetails", ignore = true)
    @Mapping(target = "jobsCount", constant = "0")
    @Mapping(target = "calculatedByName", ignore = true)
    @Mapping(target = "checkNumber", ignore = true)
    @Mapping(target = "averageHourlyRate", expression = "java(calculateAverageRate(payment))")
    PaymentResponseDTO toSimpleResponseDTO(Payment payment);

    default Integer getJobsCount(List<JobDetailsDTO> jobDetails) {
        return jobDetails != null ? jobDetails.size() : 0;
    }

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