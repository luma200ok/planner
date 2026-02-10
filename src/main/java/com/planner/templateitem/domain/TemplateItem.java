package com.planner.templateitem.domain;

import com.planner.template.domain.Template;
import com.planner.template.domain.TemplateRuleType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

@Entity
@Table(name = "template_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TemplateItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id")
    private Template template;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false)
    private TemplateRuleType ruleType;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private DayOfWeek dayOfWeek;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public TemplateItem(Template template, String name, int sortOrder) {
        this.template = template;
        this.name = name;
        this.sortOrder = sortOrder;
        this.createdAt = LocalDateTime.now();
    }

    public void setTemplate(Template template) {
        this.template = template;
    }

    public void setRuleType(TemplateRuleType ruleType) {
        this.ruleType = ruleType;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
}
