package com.planner.task.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks", indexes = {
        @Index(name = "idx_tasks_scheduled_date", columnList = "scheduledDate"),
        @Index(name = "idx_tasks_completed_at", columnList = "completedAt")
})
@Getter
@NoArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private LocalDate scheduledDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status;

    private LocalDateTime completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private TaskTemplate template;

    public Task(String title, LocalDate scheduledDate) {
        this.title = title;
        this.scheduledDate = scheduledDate;
        this.status = TaskStatus.PLANNED;
    }

    public void update(String title, LocalDate scheduledDate) {
        this.title = title;
        this.scheduledDate = scheduledDate;
    }

    public boolean isDone() {
        return this.status == TaskStatus.DONE;
    }

    public void complete(LocalDateTime now) {
        if (isDone()) return;
        this.status = TaskStatus.DONE;
        this.completedAt = now;
    }

    public void undo() {
        this.status = TaskStatus.PLANNED;
        this.completedAt = null;
    }

    public void skip(LocalDateTime now) {
        if (this.status == TaskStatus.DONE) return;
        if (this.status == TaskStatus.SKIPPED) return;
        this.status = TaskStatus.SKIPPED;
        this.completedAt = now;
    }

    public void attachTemplate(TaskTemplate template) {
        this.template = template;
    }

    public void moveScheduledDate(LocalDate newDate) {
        this.scheduledDate = newDate;
    }
}
