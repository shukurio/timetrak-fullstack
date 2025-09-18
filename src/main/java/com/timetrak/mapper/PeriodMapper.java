package com.timetrak.mapper;

import com.timetrak.dto.payment.Period;
import com.timetrak.dto.payment.PeriodSummaryDTO;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class PeriodMapper {

    public PeriodSummaryDTO toSummaryDTO(Period period) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d");
        String displayLabel = String.format("%s - %s, %d",
                period.getStartDate().format(formatter),
                period.getEndDate().format(formatter),
                period.getEndDate().getYear());

        return PeriodSummaryDTO.builder()
                .periodNumber(period.getPeriodNumber())
                .periodStart(period.getStartDate())
                .periodEnd(period.getEndDate())
                .displayLabel(displayLabel)
                .build();
    }
}