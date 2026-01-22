package com.planner.report.application.dto;

import java.time.LocalDate;

public record DailyReportRow(
        LocalDate date,
        long total,
        long done,
        long skipped,
        long planned
) {
    public double completionRate() {
        return total == 0 ? 0.0 : (double) done / total;
    }
}
