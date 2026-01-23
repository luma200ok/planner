package com.planner.report.application.dto;

import com.planner.report.application.ReportService;
import com.planner.task.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TemplateReportServiceTest {

    @Mock
    TaskRepository taskRepository;

    @InjectMocks
    ReportService reportService;

    @Test
    void oneOff_total이_0보다_크면_리스트에_추가() {
        LocalDate from = LocalDate.of(2026, 01, 01);
        LocalDate to = LocalDate.of(2026, 01, 31);

        // 탬플릿 row 1개
        when(taskRepository.templateRow(from, to)).thenReturn(List.of(
                new TemplateRow(1L, "WEEKDAYS", "WEEKDAYS",
                        10L, 7L, 2L, 1L)
        ));

        when(taskRepository.oneOffRow(0L, from, to)).thenReturn(
                new TemplateRow(0L, "ONE_OFF", "ONE_OFF",
                        3L, 1L, 1L, 1L)
        );

        var result = reportService.getTemplates(from, to);

        assertThat(result).hasSize(2);
        assertThat(result).anyMatch(
                r -> r.templateId().equals(0L) && r.templateName().equals("ONE_OFF"));
    }
}
