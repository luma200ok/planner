package com.planner.application;

import com.planner.domain.Task;
import com.planner.domain.TaskStatus;
import com.planner.domain.Template;
import com.planner.domain.TemplateRuleType;
import com.planner.repository.TaskRepository;
import com.planner.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PlannerService {
    private final TaskRepository taskRepository;
    private final TemplateRepository templateRepository;

    public void closeDay(LocalDate date, boolean carryOver) {
        List<Task> unfinished = taskRepository.findAllByScheduledDateAndStatus(date, TaskStatus.PLANNED);

        for (Task task : unfinished) {
            // 날짜 이동 없이 상태만 SKIPPED로 변경 (completedAt에 마감 시간 기록)
            task.skip(LocalDateTime.now());
        }

    }

    public Task createTask(String title, LocalDate date) {
        return taskRepository.save(new Task(title, date, null));
    }

    // PlannerService.java 의 completeTask 메서드를 수정
    public void completeTask(Long id) {
        Task task = taskRepository.findById(id).orElseThrow();
        task.complete(LocalDateTime.now());
    }

    @Transactional
    public void skipTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 할 일입니다."));

        // Task 엔티티의 skip 메서드 호출 (이미 만들어두셨음)
        task.skip(LocalDateTime.now());
    }

    @Transactional
    public void undoTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 할 일입니다."));

        // 완료든 스킵이든 무조건 '계획됨(PLANNED)' 상태로 리셋합니다.
        task.undoTask();
    }

    @Transactional(readOnly = true)
    // 🚩 status 인자 추가 확인
    public List<Task> getTasks(LocalDate from, LocalDate to, TaskStatus status) {
        // 🚩 기존 findAll... 대신, 방금 고친 searchTasks를 호출합니다.
        return taskRepository.searchTasks(from, to, status, null);
    }

    // 할 일 내용 수정
    public void updateTask(Long id, String newTitle) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 할 일입니다. (ID: " + id + ")"));

        // 엔티티의 필드를 업데이트 (Dirty Checking에 의해 자동 저장됨)
        task.updateTitle(newTitle);
    }

    // 할 일 삭제
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    // 🚩 selectedDate 파라미터 추가
    public void createTemplate(String title, TemplateRuleType ruleType, DayOfWeek dayOfWeek,
                               LocalDate selectedDate) {
        // 1. 템플릿 저장
        Template template = new Template(title, ruleType, dayOfWeek);
        templateRepository.save(template);

        // 2. 버그 해결 핵심: LocalDate.now() 대신 파라미터로 받은 selectedDate를 사용!
        // 만약 selectedDate가 null이면 방어 코드로 오늘 날짜 사용
        LocalDate baseDate = (selectedDate != null) ? selectedDate : LocalDate.now();

        // 3. 선택한 날짜가 속한 주의 일요일 계산
        LocalDate sunday = baseDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));

        List<Task> taskBasket = new ArrayList<>();

        // 4. 해당 주의 7일간 돌면서 생성
        for (int i = 0; i < 7; i++) {
            LocalDate targetDate = sunday.plusDays(i);

            // ruleType 설계도(matches)에게 물어봄
            if (template.matches(targetDate)) {
                taskBasket.add(new Task(template.getTitle(), targetDate, template));
            }
        }
        taskRepository.saveAll(taskBasket); // 🚚 일괄 저장!
    }

    // 매주 템플릿 걸린 작업 재생성
    // N+1 문제 쿼리를 한번에 모아 보낸다.
    @Scheduled(cron = "0 0 0 * * sun")
    @Transactional
    public void generateWeeklyTasksFromTemplates() {
        List<Template> allTemplates = templateRepository.findAll();
        LocalDate sunday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));

        // 1. 바구니(List)를 하나 준비합니다.
        List<Task> taskBasket = new ArrayList<>();

        for (Template template : allTemplates) {
            for (int i = 0; i < 7; i++) {
                LocalDate targetDate = sunday.plusDays(i);

                if (template.matches(targetDate)) {
                    // 2. DB에 바로 저장하지 않고 바구니에 차곡차곡 담습니다.
                    taskBasket.add(new Task(template.getTitle(), targetDate, template));
                }
            }
        }
        // 3. 바구니가 다 찼으면 DB에 한 번에 배달합니다!
        taskRepository.saveAll(taskBasket);
    }

}
