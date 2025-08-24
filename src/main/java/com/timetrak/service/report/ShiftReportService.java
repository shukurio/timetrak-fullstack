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
import com.timetrak.service.DepartmentService;
import com.timetrak.service.payment.PaymentPeriodService;
import com.timetrak.service.shift.ShiftService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShiftReportService {
    private final ShiftService shiftService;
    private final PaymentPeriodService periodService;
    private final DepartmentService depService;

    public byte[] exportShifts(Integer periodNumber, Long companyId, @Nullable List<Long> departmentIds) {
        PaymentPeriod period = resolvePaymentPeriod(periodNumber, companyId);

        List<Long> targetDepartments;
        String reportType;

        if (departmentIds == null || departmentIds.isEmpty()) {
            targetDepartments = depService.getAllDepartmentIdsForCompany(companyId);
            reportType = "Company-Wide";
            log.info("Exporting shifts for ALL departments in company {} from {} to {}",
                    companyId, period.getStartDate(), period.getEndDate());
        } else {
            targetDepartments = departmentIds;
            reportType = departmentIds.size() == 1 ? "Department" : "Multi-Department";
            log.info("Exporting shifts for departments {} in company {} from {} to {}",
                    departmentIds, companyId, period.getStartDate(), period.getEndDate());
        }

        Map<Long, List<ShiftResponseDTO>> shiftsByDepartment = shiftService.getShiftsByDepartmentsGrouped(
                targetDepartments, companyId, period.getStartDate(), period.getEndDate());

        Map<Long, String> departmentNames = getDepartmentNames(shiftsByDepartment.keySet(),companyId);

        if (shiftsByDepartment.isEmpty()) {
            log.warn("No shifts found for departments {} in company {} for period {}",
                    targetDepartments, companyId, period.getShortDescription());
        }

        try {
            return generateDepartmentCategorizedPdf(shiftsByDepartment, departmentNames, period, reportType);
        } catch (Exception e) {
            log.error("Failed to generate PDF for company {} departments {}: {}",
                    companyId, targetDepartments, e.getMessage(), e);
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

    private Map<Long, String> getDepartmentNames(Set<Long> departmentIds,Long companyId) {
        return departmentIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> {
                            try {
                                return depService.getDepartmentById(id,companyId).getName();
                            } catch (Exception e) {
                                log.warn("Could not get name for department {}: {}", id, e.getMessage());
                                return "Department " + id;
                            }
                        }
                ));
    }

    private byte[] generateDepartmentCategorizedPdf(
            Map<Long, List<ShiftResponseDTO>> shiftsByDepartment,
            Map<Long, String> departmentNames,
            PaymentPeriod period,
            String reportType) throws Exception {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4.rotate());

            int totalShifts = shiftsByDepartment.values().stream()
                    .mapToInt(List::size).sum();

            createPdfHeader(document, reportType + " Shift Report - " + period.getShortDescription(),
                    totalShifts, shiftsByDepartment.size());

            boolean firstDepartment = true;
            for (Map.Entry<Long, List<ShiftResponseDTO>> entry : shiftsByDepartment.entrySet()) {
                Long deptId = entry.getKey();
                List<ShiftResponseDTO> shifts = entry.getValue();
                String deptName = departmentNames.getOrDefault(deptId, "Department " + deptId);

                if (!firstDepartment) {
                    document.add(new Paragraph(" ").setMarginTop(30));
                }

                addDepartmentSection(document, deptName, shifts, deptId);
                firstDepartment = false;
            }

            createOverallSummary(document, shiftsByDepartment, departmentNames);

            document.close();
            return out.toByteArray();
        }
    }

    private void createPdfHeader(Document document, String title, int totalShifts, int departmentCount) {
        Paragraph mainTitle = new Paragraph("Shift Report")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(24)
                .setBold()
                .setMarginBottom(10);
        document.add(mainTitle);

        Paragraph subtitle = new Paragraph(title)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(16)
                .setMarginBottom(5);
        document.add(subtitle);

        Paragraph stats = new Paragraph(String.format(
                "Total Shifts: %d | Departments: %d", totalShifts, departmentCount))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(12)
                .setMarginBottom(20);
        document.add(stats);

        Paragraph timestamp = new Paragraph(String.format("Generated on: %s",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10)
                .setMarginBottom(30);
        document.add(timestamp);
    }

    private void addDepartmentSection(Document document, String deptName,
                                      List<ShiftResponseDTO> shifts, Long deptId) {
        Paragraph deptTitle = new Paragraph(String.format("Department: %s (ID: %d)", deptName, deptId))
                .setBold()
                .setFontSize(16)
                .setMarginTop(20)
                .setMarginBottom(5)
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setPadding(8)
                .setTextAlignment(TextAlignment.LEFT);
        document.add(deptTitle);

        BigDecimal deptTotalHours = shifts.stream()
                .map(ShiftResponseDTO::getTotalHours)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Paragraph deptStats = new Paragraph(String.format(
                "Shifts: %d | Total Hours: %.2f",
                shifts.size(), deptTotalHours))
                .setFontSize(12)
                .setMarginBottom(10);
        document.add(deptStats);

        Table table = createShiftTable(shifts);
        document.add(table);

        createDepartmentSummary(document, shifts, deptName);
    }

    private Table createShiftTable(List<ShiftResponseDTO> shifts) {
        String[] headers = {
                "Employee ID",
                "Employee Name",
                "Job Title",
                "Clock In",
                "Clock Out",
                "Total Hours",
                "Status"
        };

        Table table = new Table(headers.length);
        table.setWidth(UnitValue.createPercentValue(100));
        table.setMarginBottom(15);

        for (String header : headers) {
            Cell headerCell = new Cell()
                    .add(new Paragraph(header).setBold())
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(5)
                    .setFontSize(10);
            table.addHeaderCell(headerCell);
        }

        for (ShiftResponseDTO shift : shifts) {
            addShiftRowToPdf(table, shift);
        }

        return table;
    }

    private void addShiftRowToPdf(Table table, ShiftResponseDTO shift) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm");

        table.addCell(new Cell()
                .add(new Paragraph(String.valueOf(shift.getEmployeeId())))
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(4)
                .setFontSize(9));

        table.addCell(new Cell()
                .add(new Paragraph(shift.getFullName() != null ? shift.getFullName() : "Unknown"))
                .setTextAlignment(TextAlignment.LEFT)
                .setPadding(4)
                .setFontSize(9));

        table.addCell(new Cell()
                .add(new Paragraph(shift.getJobTitle() != null ? shift.getJobTitle() : "N/A"))
                .setTextAlignment(TextAlignment.LEFT)
                .setPadding(4)
                .setFontSize(9));

        table.addCell(new Cell()
                .add(new Paragraph(shift.getClockIn() != null ?
                        shift.getClockIn().format(formatter) : "N/A"))
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(4)
                .setFontSize(9));

        String clockOutText = shift.getClockOut() != null ?
                shift.getClockOut().format(formatter) : "Active";
        table.addCell(new Cell()
                .add(new Paragraph(clockOutText))
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(4)
                .setFontSize(9));

        String hoursText = shift.getTotalHours() != null ?
                String.format("%.2f", shift.getTotalHours()) : "0.00";
        table.addCell(new Cell()
                .add(new Paragraph(hoursText))
                .setTextAlignment(TextAlignment.RIGHT)
                .setPadding(4)
                .setFontSize(9));

        table.addCell(new Cell()
                .add(new Paragraph(shift.getStatus().toString()))
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(4)
                .setFontSize(9));
    }

    private void createDepartmentSummary(Document document, List<ShiftResponseDTO> shifts, String deptName) {
        BigDecimal totalHours = shifts.stream()
                .map(ShiftResponseDTO::getTotalHours)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long completedShifts = shifts.stream()
                .filter(s -> "COMPLETED".equals(s.getStatus().toString()))
                .count();

        Paragraph summary = new Paragraph(String.format(
                "%s Summary: %d total shifts, %d completed, %.2f total hours",
                deptName, shifts.size(), completedShifts, totalHours))
                .setFontSize(11)
                .setBold()
                .setMarginTop(10)
                .setMarginBottom(20)
                .setBackgroundColor(ColorConstants.YELLOW)
                .setPadding(5);
        document.add(summary);
    }

    private void createOverallSummary(Document document,
                                      Map<Long, List<ShiftResponseDTO>> shiftsByDepartment,
                                      Map<Long, String> departmentNames) {

        int totalShifts = shiftsByDepartment.values().stream()
                .mapToInt(List::size).sum();

        BigDecimal grandTotalHours = shiftsByDepartment.values().stream()
                .flatMap(List::stream)
                .map(ShiftResponseDTO::getTotalHours)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalCompletedShifts = shiftsByDepartment.values().stream()
                .flatMap(List::stream)
                .filter(s -> "COMPLETED".equals(s.getStatus().toString()))
                .count();

        document.add(new Paragraph(" ").setMarginTop(30));

        Paragraph summaryTitle = new Paragraph("Overall Summary")
                .setBold()
                .setFontSize(18)
                .setMarginBottom(15)
                .setTextAlignment(TextAlignment.CENTER)
                .setBackgroundColor(ColorConstants.DARK_GRAY)
                .setFontColor(ColorConstants.WHITE)
                .setPadding(10);
        document.add(summaryTitle);

        Paragraph deptBreakdown = new Paragraph("Department Breakdown:")
                .setBold()
                .setFontSize(14)
                .setMarginBottom(10);
        document.add(deptBreakdown);

        for (Map.Entry<Long, List<ShiftResponseDTO>> entry : shiftsByDepartment.entrySet()) {
            String deptName = departmentNames.getOrDefault(entry.getKey(), "Department " + entry.getKey());
            int deptShiftCount = entry.getValue().size();
            BigDecimal deptHours = entry.getValue().stream()
                    .map(ShiftResponseDTO::getTotalHours)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Paragraph deptLine = new Paragraph(String.format(
                    "• %s: %d shifts, %.2f hours", deptName, deptShiftCount, deptHours))
                    .setFontSize(12)
                    .setMarginLeft(20);
            document.add(deptLine);
        }

        Paragraph overallTotals = new Paragraph(String.format(
                """
                        
                        Grand Totals:
                        • Total Departments: %d
                        • Total Shifts: %d
                        • Completed Shifts: %d
                        • Total Hours: %.2f""",
                shiftsByDepartment.size(), totalShifts, totalCompletedShifts, grandTotalHours))
                .setFontSize(12)
                .setBold()
                .setMarginTop(15)
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setPadding(10);
        document.add(overallTotals);
    }
}