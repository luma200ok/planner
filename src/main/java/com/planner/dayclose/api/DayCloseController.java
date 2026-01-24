package com.planner.dayclose.api;

import com.planner.dayclose.application.DayCloseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

import static com.planner.dayclose.application.dto.DayCloseDto.*;
import static org.springframework.format.annotation.DateTimeFormat.*;

@Tag(name = "DayClose", description = "일 마감/자동 스킵/생성 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/day-close")
public class DayCloseController {

    private final DayCloseService dayCloseService;

    @PostMapping
    public DayCloseResponse close(
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "true") boolean carryOver,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate carryTo
    ) {
        return dayCloseService.close(date, carryOver, carryTo);
    }
}
