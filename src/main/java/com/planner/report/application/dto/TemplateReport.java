package com.planner.report.application.dto;

public record TemplateReport(
        Long templateId,
        String templateName,
        String rule,
        long total,
        long done,
        long skipped,
        long planned,
        double completionRate // 0~100%
) {
    public static TemplateReport of(Long templateId, String templateName, String rule,
                                    long total, long done, long skipped, long planned) {
        double rate = total == 0 ? 0.0 : (done*100.0) / total;
        rate = Math.round(rate * 10.0) / 10.0;
        return new TemplateReport(templateId, templateName, rule, total, done, skipped, planned, rate);
    }
}
