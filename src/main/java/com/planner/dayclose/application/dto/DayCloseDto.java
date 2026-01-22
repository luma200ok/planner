package com.planner.dayclose.application.dto;

import java.time.LocalDate;

public class DayCloseDto {

    public record DayCloseResponse(
            LocalDate closedDate,
            LocalDate nextDate,
            boolean carryOver,
            int plannedFound,
            int autoSkippedRecurring,
            int rolledOverOneOff,
            int autoSkippedOneOff,
            int generatedNext
            ) {
    }
}
