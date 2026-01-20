package com.planner.task.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateRequest(
        @NotBlank(message = "title은 필수입니다.")
        String title,

        @NotNull(message = "scheduledDate는 필수입니다.")
        LocalDate scheduledDate
        ) {
}
