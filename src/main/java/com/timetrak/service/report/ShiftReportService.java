package com.timetrak.service.report;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.timetrak.dto.payment.PaymentPeriod;
import com.timetrak.dto.response.ShiftResponseDTO;
import com.timetrak.service.payment.PaymentPeriodService;
import com.timetrak.service.shift.ShiftService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShiftReportService {
    private final ShiftService shiftService;
    private final PaymentPeriodService periodService;

    public byte[] exportShifts(int periodNumber,Long companyId) {

        PaymentPeriod period = resolvePaymentPeriod(periodNumber,companyId);
        log.info("Exporting shifts for company {} from {} to {} in PDF format",
                companyId, period.getStartDate(), period.getEndDate());


        List<ShiftResponseDTO> shifts = shiftService.getShiftsByDateRange(companyId,
                period.getStartDate(),
                period.getEndDate());

        if (shifts.isEmpty()) {
            log.warn("No shifts found for company {} in date range {} to {}", companyId, period.getStartDate(), period.getEndDate());
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4.rotate());

            //title
            createPdfHeader(document, period.getShortDescription(), shifts.size());

            //add table
            Table table = createShiftTable(shifts);
            document.add(table);

            //add summary
            createPdfSummary(document, shifts);

            document.close();
            log.info("Generated PDF file with {} shifts", shifts.size());
            return out.toByteArray();

        } catch (Exception e) {
            log.error("Failed to generate PDF file for company {}: {}", companyId, e.getMessage(), e);
            throw new RuntimeException("Failed to generate PDF file", e);
        }
    }

    private PaymentPeriod resolvePaymentPeriod(Integer periodNumber, Long companyId) {
        if (periodNumber == null || periodNumber <= 0) {
            log.info("No period number provided, using current period for company {}", companyId);
            return periodService.getCurrentPaymentPeriod(companyId);
        } else {
            log.info("Using period {} for company {}", periodNumber, companyId);
            return periodService.getPaymentPeriodByNumber(periodNumber, companyId);
        }
    }


    private void createPdfHeader(Document document,String formattedPeriod, int shiftCount) {
        //Main title
        Paragraph title = new Paragraph("Shift Report")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(24)
                .setBold()
                .setMarginBottom(10);
        document.add(title);

        //Date range
        Paragraph dateRange = new Paragraph("Period: " + formattedPeriod)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(14)
                .setMarginBottom(5);
        document.add(dateRange);

        //Shift count
        Paragraph count = new Paragraph(String.format("Total Shifts: %d", shiftCount))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(12)
                .setMarginBottom(20);
        document.add(count);
    }

    private Table createShiftTable(List<ShiftResponseDTO> shifts) {
        //create table with 6 columns

        //cdd headers
        String[] headers = {
                "Employee ID",
                "Employee Name",
                "Clock In",
                "Clock Out",
                "Total Hours",
                "Status"
        };
        Table table = new Table(headers.length);
        table.setWidth(UnitValue.createPercentValue(100));

        for (String header : headers) {
            Cell headerCell = new Cell()
                    .add(new Paragraph(header).setBold())
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(8);
            table.addHeaderCell(headerCell);
        }

        //add data rows
        for (ShiftResponseDTO shift : shifts) {
            addShiftRowToPdf(table, shift);
        }

        return table;
    }

    private void addShiftRowToPdf(Table table, ShiftResponseDTO shift) {
        // Employee ID
        table.addCell(new Cell().add(new Paragraph(String.valueOf(shift.getEmployeeId())))
                .setTextAlignment(TextAlignment.CENTER));

        // Employee Name
        table.addCell(new Cell().add(new Paragraph(shift.getFullName()))
                .setTextAlignment(TextAlignment.CENTER));

        // Clock In
        table.addCell(new Cell().add(new Paragraph(shift.getClockIn().toString())));

        // Clock Out
        table.addCell(new Cell().add(new Paragraph(shift.getClockOut().toString()))
                .setTextAlignment(TextAlignment.RIGHT));

        // Total Hours
        table.addCell(new Cell().add(new Paragraph("$" + shift.getTotalHours().toString()))
                .setTextAlignment(TextAlignment.RIGHT));

        // Status
        table.addCell(new Cell().add(new Paragraph(shift.getStatus().toString()))
                .setTextAlignment(TextAlignment.CENTER));

    }

    private void createPdfSummary(Document document, List<ShiftResponseDTO> shifts) {

        BigDecimal totalHours = shifts.stream()
                .map(ShiftResponseDTO::getTotalHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Add summary section
        document.add(new Paragraph(" ").setMarginTop(20)); // Space

        Paragraph summaryTitle = new Paragraph("Summary")
                .setBold()
                .setFontSize(16)
                .setMarginBottom(10);
        document.add(summaryTitle);

        Paragraph summaryText = new Paragraph(String.format(
                "Total Shifts: %d \nTotal Earnings: $%s",
                shifts.size(), totalHours))
                .setFontSize(12);
        document.add(summaryText);

        //add generation timestamp
        Paragraph timestamp = new Paragraph(String.format("Generated on: %s",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontSize(10)
                .setMarginTop(20);
        document.add(timestamp);
    }
}
