package com.planner.task.domain;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "day_close_logs",
        uniqueConstraints = @UniqueConstraint(name = "uk_day_close_logs_closed_date", columnNames = "closed_date"))
@NoArgsConstructor
@Getter
public class DayCloseLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate closedDate;

    @Column(nullable = false)
    private LocalDateTime executedAt;

    public DayCloseLog(LocalDate closedDate, LocalDateTime executedAt) {
        this.closedDate = closedDate;
        this.executedAt = executedAt;
    }
}
