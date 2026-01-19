package com.planner.global.task.application.dto;

import com.planner.global.task.domain.Task;

import java.time.LocalDate;

public record TaskResponse(
        Long id,
        String title,
        LocalDate scheduledDate) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(task.getId(), task.getTitle(), task.getScheduledDate());
    }
}