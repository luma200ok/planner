package com.planner.report.api;

import com.planner.report.application.ReportService;
import com.planner.report.application.dto.DailyReportRow;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.format.annotation.DateTimeFormat.*;


@Tag(name = "Reports")
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/dialy")
    public List<DailyReportRow> daily(
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate to
    ) {
        return reportService.getDaily(from, to);
    }
}
