package com.planner.task.application.dto;

import java.time.LocalDate;

public class DayCloseDto {

    public record DayCloseResponse(
            LocalDate closedDate,
            LocalDate carryTo,
            boolean carryOver,
            int plannedFound,
            int autoSkippedRecurring,
            int rollOverOneOff,
            int autoSkippedOneOff,
            int generatedNext
            ) {
    }
}
