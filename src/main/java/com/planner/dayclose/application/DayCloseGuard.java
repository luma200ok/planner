package com.planner.dayclose.application;

import com.planner.dayclose.repository.DayCloseLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DayCloseGuard {

    private final DayCloseLogRepository logRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean tryAcquire(LocalDate closedDate) {
        int inserted = logRepository.insertIgnore(closedDate, LocalDateTime.now());
        return inserted == 1;
    }
}
