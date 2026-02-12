package com.planner.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

public enum TemplateRuleType {
    NONE ,DAILY, WEEKDAYS, WEEKENDS, WEEKLY;

    public boolean matches(LocalDate date, DayOfWeek targetDay) {
        return switch (this) {
            case DAILY -> true;
            case NONE -> false;
            case WEEKDAYS -> !Set.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY).contains(date.getDayOfWeek());
            case WEEKENDS -> Set.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY).contains(date.getDayOfWeek());
            case WEEKLY -> date.getDayOfWeek() == targetDay;
        };
    }
}
