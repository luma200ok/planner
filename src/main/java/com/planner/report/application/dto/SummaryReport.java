package com.planner.report.application.dto;

public record SummaryReport(
        long total,
        long done,
        long skipped,
        long planned,
        double completionRate,
        double skipRate
) {
    public static SummaryReport of(long total, long done, long skipped, long planned) {
        double completionRate = total == 0 ? 0.0 : (double) done / total;
        double skipRate = total == 0 ? 0.0 : (double) skipped / total;
        return new SummaryReport(total, done, skipped, planned, completionRate, skipRate);
    }
}
