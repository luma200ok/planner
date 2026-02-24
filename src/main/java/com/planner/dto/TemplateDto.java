package com.planner.dto;

import com.planner.domain.TemplateRuleType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class TemplateDto {
    public record TemplateCreateRequest(
            @Schema(description = "템플릿 제목", example = "매일 아침 운동")
            String title,
            @Schema(description = "반복 규칙 (DAILY, WEEKDAYS, WEEKENDS, WEEKLY)", example = "DAILY")
            TemplateRuleType ruleType,
            @Schema(description = "반복 요일 (WEEKLY일 때 사용)", example = "MONDAY")
            DayOfWeek dayOfWeek,
            @Schema(description = "기준 시작 날짜", example = "2026-02-24")
            LocalDate date // 🚩 프론트엔드의 "2026-02-19"가 이 필드로 들어옵니다.
    ) {}

    public record TemplateResponse(
            @Schema(description = "Template 고유 ID")
            Long id,
            @Schema(description = "Template 제목")
            String title,
            @Schema(description = "Template 규칙")
            TemplateRuleType ruleType,
            @Schema(description = "Template 요일")
            java.time.DayOfWeek dayOfWeek,
            @Schema(description = "Template On/Off ,미적용")
            boolean active
    ) {}

    public record TemplateUpdateRequest(
            String title,
            TemplateRuleType ruleType,
            DayOfWeek dayOfWeek) {}

}
