package com.planner.global.error.api;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequestMapping("/test")
public class TestErrorController {

    @GetMapping("/bad")
    public void bad() {
        throw new IllegalArgumentException("400 에러 발생");
    }

    @PostMapping("/validate")
    public String validate(@Valid @RequestBody Req req) {
        return "ok";
    }

    public record Req(@NotBlank(message = "name은 필수입니다.") String name) {}
}
