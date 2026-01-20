package com.planner.task.application.dto;

import com.planner.task.domain.Task;
import com.planner.task.domain.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String title,
        LocalDate scheduledDate,
        TaskStatus status,
        LocalDateTime completedAt) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getScheduledDate(),
                task.getStatus(),
                task.getCompletedAt()
        );
    }
}