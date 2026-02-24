package com.planner.dto;

import com.planner.domain.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public class TaskDto {


    public record CreateRequest(
            @Schema(description = "할 일 제목", example = "운동하기")
            String title,
            @Schema(description = "마감 날짜", example = "2026-02-12")
            LocalDate date
    ) {}

    public record UpdateRequest(
            @Schema(description = "변경할 할 일 제목", example = "매일 30분 운동하기")
            String title
    ) {}


    public record TaskResponse(
            @Schema(description = "Task 고유 ID")
            Long id,
            @Schema(description = "할 일 제목")
            String title,
            @Schema(description = "현재 상태 (PLANNED, DONE, SKIPPED)")
            TaskStatus status,
            @Schema(description = "예정된 날짜")
            LocalDate date
    ) {}

}
