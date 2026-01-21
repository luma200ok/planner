package com.planner.task.application;

import com.planner.task.domain.DayCloseLog;
import com.planner.task.repository.DayClosLogRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DayCloseGuard {

    private final DayClosLogRepository logRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean tryAcquire(LocalDate closedDate) {
        int inserted = logRepository.insertIgnore(closedDate, LocalDateTime.now());
        return inserted == 1;
    }
}
