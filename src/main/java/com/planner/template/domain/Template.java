package com.planner.template.domain;

import com.planner.Tempaltem.domain.TemplateItem;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "task_templates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Template {

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

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder asc, id asc")
    private List<TemplateItem> items = new ArrayList<>();

    public Template(String title, TemplateRuleType ruleType) {
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
