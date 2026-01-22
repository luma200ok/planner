package com.planner.report.application;

import com.planner.report.application.dto.DailyReportRow;
import com.planner.report.application.dto.SummaryReport;
import com.planner.report.application.dto.SummaryRow;
import com.planner.report.application.dto.TemplateReportRow;
import com.planner.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TaskRepository taskRepository;

    public List<DailyReportRow> getDaily(LocalDate from, LocalDate to) {
        return taskRepository.dailyReport(from, to);
    }

    public SummaryReport getSummary(LocalDate from, LocalDate to) {
        SummaryRow row = taskRepository.summary(from, to);
        return SummaryReport.of(row.total(), row.done(), row.skipped(), row.planned());
    }

    public List<TemplateReportRow> getTemplates(LocalDate from, LocalDate to) {
        return taskRepository.templateReport(from, to);
    }
}
