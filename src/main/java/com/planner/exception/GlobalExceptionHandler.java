package com.planner.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. 직접 던진 에러 (ex. Task 못 찾을때)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        ErrorResponse error = new ErrorResponse(e.getMessage(), "INVALID_INPUT");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // 2. 모든 예상치 못한 에러 (서버 내부 에러)
    public ResponseEntity<ErrorResponse> handleAllException(Exception e) {
        ErrorResponse error = new ErrorResponse("서버 내부 오류가 발생했습니다.", "SERVER_ERROR");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
