package com.planner.task.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateRequest(
        @NotBlank(message = "title은 필수입니다.")
        String title,
        LocalDate scheduledDate) {
}


