package com.planner.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
public class Template {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;

    @Enumerated(EnumType.STRING)
    private TemplateRuleType ruleType; // DAILY, WEEKLY 등

    private DayOfWeek dayOfWeek;
    private boolean active = true;

    public boolean matches(LocalDate date) {
        return ruleType.matches(date, this.dayOfWeek); // Enum 내의 로직 활용
    }

    public Template(String title, TemplateRuleType ruleType, DayOfWeek dayOfWeek) {
        this.title = title;
        this.ruleType = ruleType;
        this.dayOfWeek = dayOfWeek;
        this.active = true; // 기본적으로 활성화 상태
    }

}
