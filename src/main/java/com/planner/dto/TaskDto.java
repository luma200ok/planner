package com.planner.dto;

import com.planner.domain.TaskStatus;

import java.time.LocalDate;

public class TaskDto {

    public record CreateRequest(String title, LocalDate date) {}
    public record UpdateRequest(String title) {}

    public record TaskResponse(Long id, String title, TaskStatus status, LocalDate date) {}
}
