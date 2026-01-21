package com.planner.task.scheduler;

import com.planner.task.application.DayCloseGuard;
import com.planner.task.application.DayCloseService;
import com.planner.task.application.dto.DayCloseDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class DayCloseScheduler {

    private final DayCloseService dayCloseService;
    private final DayCloseGuard dayCloseGuard;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void run() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        if (!dayCloseGuard.tryAcquire(yesterday)) {
            log.info("[DAY_CLOSE] already executed. date={}", yesterday);
            return;
        }
        var result = dayCloseService.close(yesterday, true, null);

        log.info("[DAY_CLOSE] closedDate={}, nextDate={}, plannedFound={}, autoSkippedRecurring={}, rolledOverOneOff={}, autoSkippedOneOff={}, generatedNext={}",
                result.closedDate(),
                result.nextDate(),
                result.plannedFound(),
                result.autoSkippedRecurring(),
                result.rolledOverOneOff(),
                result.autoSkippedOneOff(),
                result.generatedNext()
        );
    }
}
