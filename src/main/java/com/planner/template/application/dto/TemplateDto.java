package com.planner.template.application.dto;

import com.planner.template.domain.TemplateRuleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;

public class TemplateDto {

    public record CreateRequest(
            @NotBlank String title,
            @NotNull TemplateRuleType ruleType,
            DayOfWeek dayOfWeek // WEEKLY 일때만 필수
    ) {}

    public record TemplateResponse(
            Long id,
            String title,
            TemplateRuleType ruleType,
            boolean active
    ) {
        public static TemplateResponse from(Long id, String title, TemplateRuleType ruleType, boolean active) {
            return new TemplateResponse(id, title, ruleType, active);
        }
    }
}
