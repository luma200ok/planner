package com.planner.application;

import com.planner.domain.Task;
import com.planner.domain.TemplateRuleType;
import com.planner.repository.TaskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional // 테스트 후 데이터를 자동 롤백해줍니다.
class PlannerServiceTest {

    @Autowired private PlannerService plannerService;
    @Autowired private TaskRepository taskRepository;

    @Test
    @DisplayName("새로운 할 일을 생성하면 DB에 정상 저장되어야 한다")
    void createTaskTest() {
        // given (준비)
        String title = "JUnit 공부하기";
        LocalDate date = LocalDate.now();

        // when (실행)
        plannerService.createTask(title, date);

        // then (검증)
        List<Task> tasks = taskRepository.findAll();
        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getTitle()).isEqualTo(title);
        assertThat(tasks.get(0).getScheduledDate()).isEqualTo(date);
    }

    @Test
    @DisplayName("DAILY 템플릿을 생성하면 이번 주 남은 날짜만큼 할 일이 생성되어야 한다")
    void createDailyTemplateTest() {
        // Given: '운동하기'라는 매일 반복 템플릿 정보 준비
        String title = "운동하기";
        TemplateRuleType rule = TemplateRuleType.DAILY;
        DayOfWeek dayOfWeek = null;
        LocalDate selectedDate = LocalDate.now();

        // When: 템플릿 생성 메서드 호출
        plannerService.createTemplate(title, rule,dayOfWeek ,selectedDate);

        // Then: 실제로 할 일(Task)이 생성되었는지 검증
        List<Task> tasks = taskRepository.findAll();

        // 1. 최소 1개 이상의 할 일이 생성되었는가?
        assertThat(tasks).isNotEmpty();

        // 2. 생성된 모든 할 일의 제목이 '운동하기'인가?
        assertThat(tasks).allSatisfy(task -> {
            assertThat(task.getTitle()).isEqualTo(title);
        });

        System.out.println("생성된 할 일 개수: " + tasks.size());
    }

    @Test
    @DisplayName("WEEKLY 템플릿을 특정 요일로 생성하면 해당 요일에만 할 일이 생성되어야 한다")
    void createWeeklyTemplateTest() {
        // Given: 매주 '금요일'에만 '청소하기' 템플릿 설정
        String title = "청소하기";
        TemplateRuleType rule = TemplateRuleType.WEEKLY;
        DayOfWeek targetDay = DayOfWeek.FRIDAY;
        LocalDate selectedDate = LocalDate.now();

        // When
        plannerService.createTemplate(title, rule, targetDay,selectedDate);

        // Then
        List<Task> tasks = taskRepository.findAll();

        // 생성된 모든 할 일이 실제로 '금요일'인지 확인
        assertThat(tasks).allSatisfy(task -> {
            assertThat(task.getScheduledDate().getDayOfWeek()).isEqualTo(DayOfWeek.FRIDAY);
        });
    }

}