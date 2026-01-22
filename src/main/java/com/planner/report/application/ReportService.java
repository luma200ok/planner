package com.planner.report.application;

import com.planner.report.application.dto.DailyReportRow;
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
}
