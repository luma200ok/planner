package com.planner.report.application.dto;

public record SummaryRow(
        long total,
        long done,
        long skipped,
        long planned
) {
}
