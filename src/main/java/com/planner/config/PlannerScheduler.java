package com.planner.config;

import com.planner.application.PlannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlannerScheduler {

    private final PlannerService plannerService;

    // 매주 일요일 0시에 자동으로 서비스의 로직을 호출!
    @Scheduled(cron = "0 0 0 * * SUN")
    public void runWeeklyTaskGenerator() {
        plannerService.generateWeeklyTasksFromTemplates();
    }
}
