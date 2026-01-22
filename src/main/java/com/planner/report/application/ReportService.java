package com.planner.report.application;

import com.planner.report.application.dto.DailyReportRow;
import com.planner.report.application.dto.SummaryReport;
import com.planner.report.application.dto.SummaryRow;
import com.planner.report.application.dto.TemplateReport;
import com.planner.report.application.dto.TemplateRow;
import com.planner.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Cast;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
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

    public List<TemplateReport> getTemplates(LocalDate from, LocalDate to) {
        var rows = taskRepository.templateRow(from, to).stream().map(
                r -> TemplateReport.of(
                        r.templateId(),
                        r.templateName(),
                        String.valueOf(r.rule()),
                        r.total(), r.done(), r.skipped(), r.planned())
        ).toList();

        var mutable = new ArrayList<>(rows);

        var oneOff = taskRepository.oneOffRow(0L, from, to);
        if (oneOff.total() > 0) {
            mutable.add(TemplateReport.of(
                    oneOff.templateId(),
                    oneOff.templateName(),
                    String.valueOf(oneOff.rule()),
                    oneOff.total(), oneOff.done(), oneOff.skipped(), oneOff.planned())
            );
        }
        return mutable;
    }
}
