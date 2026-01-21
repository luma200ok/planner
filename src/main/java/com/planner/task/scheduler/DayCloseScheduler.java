package com.planner.task.scheduler;

import com.planner.task.application.DayCloseService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DayCloseScheduler {

    private DayCloseService dayCloseService;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void run() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        dayCloseService.close(yesterday, true, null);
    }
}
