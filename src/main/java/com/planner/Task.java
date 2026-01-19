package com.planner;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private LocalDate scheduledDate;

    protected Task() {
    }

    public Task(String title, LocalDate scheduledDate) {
        this.title = title;
        this.scheduledDate = scheduledDate;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }
}