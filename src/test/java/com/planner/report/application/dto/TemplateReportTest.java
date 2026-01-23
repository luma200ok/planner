package com.planner.report.application.dto;

import org.assertj.core.api.Assertions;
import org.hibernate.sql.Template;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class TemplateReportTest {

    @Test
    void completionRate_total이_0이면_0이다() {
        TemplateReport r = TemplateReport.of(
                1L, "WEEKDAYS", "WEEKDAYS",
                0L, 0L, 0L, 0L);

        assertThat(r.completionRate()).isEqualTo(0);
    }

    @Test
    void completionRate_done_total_비율_계산() {
        TemplateReport r = TemplateReport.of(1L, "WEEKDAYS", "WEEKDAYS",
                3L, 1L, 1L, 1L);
        assertThat(r.completionRate()).isEqualTo(33.3);
    }

}
