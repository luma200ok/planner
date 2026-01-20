package com.planner.task.application.dto;

import com.planner.task.domain.Task;
import com.planner.task.domain.TaskStatus;
import com.planner.task.domain.TaskTemplate;
import com.planner.task.domain.TemplateRuleType;
import com.planner.task.event.TaskEvent;
import com.planner.task.event.TaskEventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TaskDto {

    public record CreateRequest(
            @NotBlank(message = "title은 필수입니다.")
            String title,
            LocalDate scheduledDate) {
    }

    public record TaskEventResponse(
            Long eventId,
            Long taskId,
            TaskEventType type,
            LocalDateTime occurredAt,
            String idempotencyKey,
            String meta
    ) {

        public static TaskEventResponse from(TaskEvent e) {
            return new TaskEventResponse(e.getId(), e.getTaskId(), e.getType(),
                    e.getOccurredAt(),e.getIdempotencyKey(), e.getMeta());
        }

    }

    public record TaskResponse(
            Long id,
            String title,
            LocalDate scheduledDate,
            TaskStatus status,
            LocalDateTime completedAt,
            Long templateId,
            TemplateRuleType ruleType,
            DayOfWeek templateDayOfWeek
    ) {
        public static TaskResponse from(Task task) {
            TaskTemplate template = task.getTemplate();
            return new TaskResponse(
                    task.getId(),
                    task.getTitle(),
                    task.getScheduledDate(),
                    task.getStatus(),
                    task.getCompletedAt(),
                    template == null ? null : template.getId(),
                    template == null ? null : template.getRuleType(),
                    template == null ? null : template.getDayOfWeek()
            );
        }

    }

    public record UndoRequest(String reason) {}

    public record UpdateRequest(
            @NotBlank(message = "title은 필수입니다.")
            String title,

            @NotNull(message = "scheduledDate는 필수입니다.")
            LocalDate scheduledDate
    ) {

    }

    public record SkipRequest(String reason) {}

}
