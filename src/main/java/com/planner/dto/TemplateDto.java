package com.planner.dto;

import com.planner.domain.TemplateRuleType;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class TemplateDto {
    public record TemplateCreateRequest(
            String title,
            TemplateRuleType ruleType,
            DayOfWeek dayOfWeek,
            LocalDate date // 🚩 프론트엔드의 "2026-02-19"가 이 필드로 들어옵니다.
    ) {}
    public record TemplateResponse(Long id, String title, TemplateRuleType ruleType,
                                   java.time.DayOfWeek dayOfWeek, boolean active) {}
    public record TemplateUpdateRequest(String title, TemplateRuleType ruleType, DayOfWeek dayOfWeek) {}

}
