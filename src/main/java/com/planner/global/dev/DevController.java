package com.planner.global.dev;

import com.planner.dayclose.application.DayCloseGuard;
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

@Tag(name = "Dev", description = "개발/운영 편의용")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/jobs")
public class DevController {

    private final DayCloseService dayCloseService;
    private final DayCloseGuard closeGuard;

    /**
     * 스케줄러 day-close를 수동 호출 (개발TEST)
     * 기본 정책: carryOver=true, carryTo=null(=date+1)
     */
    @PostMapping("/day-close/run")
    public DayCloseResponse runDayClose(
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate date) {
        if (!closeGuard.tryAcquire(date)) {
            return new DayCloseResponse(date, date.plusDays(1), true,
                    0, 0, 0, 0,0);
        }
        return dayCloseService.close(date, true, null);
    }

    @PostMapping("/day-close/run-yesterday")
    public DayCloseResponse runYesterday() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        if (!closeGuard.tryAcquire(yesterday)) {
            return new DayCloseResponse(yesterday, yesterday.plusDays(1), true,
                    0, 0, 0, 0,0);
        }
        return dayCloseService.close(yesterday, true, null);
    }
}
