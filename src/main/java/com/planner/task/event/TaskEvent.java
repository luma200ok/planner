package com.planner.task.event;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_events", indexes = {
        @Index(name = "idx_task_events_task_id", columnList = "taskId"),
        @Index(name = "idx_task_events_occurred_at", columnList = "occurredAt")
        },
        uniqueConstraints ={
        @UniqueConstraint(name = "uk_task_events_idempotency_key", columnNames = "idempotencyKey")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long taskId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskEventType type;

    @Column(nullable = false)
    private LocalDateTime occurredAt;

    @Column(length = 80)
    private String idempotencyKey;

    @Column(length = 500)
    private String meta;

    public static TaskEvent of(Long taskId, TaskEventType type, LocalDateTime occurredAt,
                               String idempotencyKey, String meta) {
        TaskEvent e = new TaskEvent();
        e.taskId = taskId;
        e.type = type;
        e.occurredAt = occurredAt;
        e.idempotencyKey = idempotencyKey;
        e.meta = meta;
        return e;
    }
}
