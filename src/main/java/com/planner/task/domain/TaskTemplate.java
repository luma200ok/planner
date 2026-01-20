package com.planner.task.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Entity
@Table(name = "task_templates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TemplateRuleType ruleType;

    @Column(nullable = false)
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private DayOfWeek dayOfWeek;

    public TaskTemplate(String title, TemplateRuleType ruleType) {
        this.title = title;
        this.ruleType = ruleType;
    }

    public boolean matches(LocalDate date) {
        return switch (ruleType) {
            case DAILY -> true;
            case WEEKDAYS -> {
                DayOfWeek d = date.getDayOfWeek();
                yield d != DayOfWeek.SATURDAY && d != DayOfWeek.SUNDAY;
            }
            case WEEKENDS -> {
                DayOfWeek d = date.getDayOfWeek();
                yield d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY;
            }
            case WEEKLY -> date.getDayOfWeek() == this.dayOfWeek;
        };
    }
}
