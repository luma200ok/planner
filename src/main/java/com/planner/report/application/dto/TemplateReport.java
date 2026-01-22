package com.planner.report.application.dto;

public record TemplateReport(
        Long templateId,
        String templateName,
        String rule,
        long total,
        long done,
        long skipped,
        long planned,
        double completionRate
) {
    public static TemplateReport of(Long templateId, String templateName, String rule,
                                    Long total, long done, long skipped, long planned) {
        double rate = total == 0 ? 0.0 : (double) done / total;
        return new TemplateReport(templateId, templateName, rule, total, done, skipped, planned, rate);
    }
}
