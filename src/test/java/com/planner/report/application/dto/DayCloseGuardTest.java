package com.planner.report.application.dto;

import com.planner.dayclose.application.DayCloseGuard;
import com.planner.dayclose.domain.DayCloseLog;
import com.planner.dayclose.repository.DayCloseLogRepository;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DayCloseGuardTest {

    @Mock
    DayCloseLogRepository logRepository;

    @InjectMocks
    DayCloseGuard dayCloseGuard;

    @Test
    void 처음이면_True_리턴() {
        when(logRepository.insertIgnore(any(LocalDate.class),
                any(LocalDateTime.class))).thenReturn(1);

        boolean acquired = dayCloseGuard.tryAcquire(LocalDate.of(2026, 1, 21));

        assertThat(acquired).isTrue();
    }

    @Test
    void 중복이면_false_리턴() {
        when(logRepository.insertIgnore(any(LocalDate.class),
                any(LocalDateTime.class))).thenReturn(0);

        boolean acquired = dayCloseGuard.tryAcquire(LocalDate.of(2026, 1, 21));

        assertThat(acquired).isFalse();
    }

}
