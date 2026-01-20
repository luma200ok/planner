package com.planner.task.application.dto;

import com.planner.task.event.TaskEvent;
import com.planner.task.event.TaskEventType;

import java.time.LocalDateTime;

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
