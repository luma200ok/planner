package com.planner.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ErrorResponse(
        @Schema(description = "에러 메시지", example = "잘못된 입력입니다.")
        String message,
        @Schema(description = "에러 코드", example = "INVALID_INPUT")
        String code,
        @Schema(description = "발생 시각")
        LocalDateTime timestamp
) {
    public ErrorResponse(String message, String code) {
        this(message, code, LocalDateTime.now());
    }
}
