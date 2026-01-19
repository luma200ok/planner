package com.planner.global.error;

import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        List<String> errors) {

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, null);
    }

    public static ErrorResponse of(String code, String message, List<String> errors) {
        return new ErrorResponse(code, message, errors);
    }
}
