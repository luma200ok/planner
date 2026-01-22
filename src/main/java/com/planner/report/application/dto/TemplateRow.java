package com.planner.report.application.dto;

public record TemplateRow(
        Long templateId,
        String templateName,
        String rule,
        Long total,
        Long done,
        Long skipped,
        Long planned
) {
}
