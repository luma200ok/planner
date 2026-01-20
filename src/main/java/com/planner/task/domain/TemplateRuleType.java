package com.planner.task.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;

public enum TemplateRuleType {
    DAILY{
        @Override
        public boolean matches(LocalDate date) {
            return true;
        }
    },
    WEEKDAYS {
        @Override
        public boolean matches(LocalDate date) {
            DayOfWeek d = date.getDayOfWeek();
            return d != DayOfWeek.SATURDAY && d != DayOfWeek.SUNDAY;
        }
    },
    WEEKENDS{
        @Override
        public boolean matches(LocalDate date) {
            DayOfWeek d = date.getDayOfWeek();
            return d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY;
        }
    }
    , WEEKLY{
        @Override
        public boolean matches(LocalDate date) {
            return false;
        }
    };

    public abstract boolean matches(LocalDate date);
}
