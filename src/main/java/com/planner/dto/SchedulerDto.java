package com.planner.dto;

import java.time.LocalDate;

public class SchedulerDto {

    public record CloseRequest(LocalDate date, boolean carryOver) {}
}
