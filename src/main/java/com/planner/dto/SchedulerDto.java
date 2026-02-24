package com.planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public class SchedulerDto {

    public record CloseRequest(
            @Schema(description = "마감할 날짜", example = "2026-02-24")
            LocalDate date,
            @Schema(description = "미완료 일정 다음날 이월 여부", example = "false")
            boolean carryOver
    ) {}
}
