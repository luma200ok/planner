package com.planner.report.application.dto;

public record TemplateReportRow(
        Long templateId,
        String templateName,
        String rule,
        long total,
        long done,
        long skipped,
        long planned
) {
    public double completionRate() {
        return total == 0 ? 0.0 : (double) done / total;
    }
}
