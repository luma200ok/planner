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

    // 🚩 1. 템플릿 생성 시 7일치 생성 / 스케쥴러를 통해 매주 일요일 00시에 갱신
    public void createTemplate(String title, TemplateRuleType ruleType, DayOfWeek dayOfWeek,
                               LocalDate selectedDate) {
        // 1. 템플릿 저장
        Template template = new Template(title, ruleType, dayOfWeek);
        templateRepository.save(template);

        // 2. 선택한 날짜(마감 날짜)가 속한 주의 일요일 계산 (이 주부터 시작!)
        LocalDate baseDate = (selectedDate != null) ? selectedDate : LocalDate.now();
        LocalDate sunday = baseDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));

        List<Task> taskBasket = new ArrayList<>();

        // 3. 7일 생성 -> 지연 생성 원칙으로 매주 일요일 00 시에 새로 갱신
        for (int i = 0; i < 7; i++) {
            LocalDate targetDate = sunday.plusDays(i);

            // ruleType 설계도에게 해당 요일이 맞는지 물어봄
            if (template.matches(targetDate)) {
                taskBasket.add(new Task(template.getTitle(), targetDate, template));
            }
        }
        taskRepository.saveAll(taskBasket); // 일괄 저장
    }

    // 매주 템플릿 걸린 작업 재생성
    // N+1 문제 쿼리를 한번에 모아 보낸다.
    @Scheduled(cron = "0 0 0 * * sun")
    @Transactional
    public void generateWeeklyTasksFromTemplates() {
        List<Template> allTemplates = templateRepository.findAllByActiveTrue();

        // 1. 오늘이 포함된 주의 일요일을 찾습니다. (주의 시작점)
        LocalDate thisSunday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));

        // 2. 🚩 핵심: 무조건 다음 주 일요일부터 시작하도록 7일을 더합니다.
        // 이렇게 하면 월요일에 누르든 토요일에 누르든 항상 '다음 주 일요일'이 시작점이 됩니다.
        LocalDate nextSunday = thisSunday.plusDays(7);

        List<Task> taskBasket = new ArrayList<>();

        for (Template template : allTemplates) {
            for (int i = 0; i < 7; i++) {
                LocalDate targetDate = nextSunday.plusDays(i);

                if (template.matches(targetDate)) {
                    boolean isExist = taskRepository.findByTemplateAndScheduledDate(template, targetDate).isPresent();
                    if (!isExist) {
                        taskBasket.add(new Task(template.getTitle(), targetDate, template));
                    }
                }
            }
        }
        taskRepository.saveAll(taskBasket);
    }

    @Transactional(readOnly = true)
    public List<Template> getAllTemplates() {
        return templateRepository.findAll();
    }
    // 템플릿 삭제 (Template <-> Task 연관 관계 끊어서 DB에서 삭제 되는거 방지)
    @Transactional
    public void deleteTemplate(Long id) {
        // 1. 삭제할 템플릿 존재 확인
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 템플릿입니다. ID: " + id));

        // 2. 핵심 로직: 이 템플릿으로 생성된 모든 할 일들과의 관계를 끊음
        // 템플릿(설계도)이 삭제되어도 할 일(자산)은 남겨두기 위함입니다.
        template.disconnectTasks();

        // 3. 관계가 다 끊어졌으므로 이제 안전하게 템플릿만 삭제
        templateRepository.delete(template);
    }

    @Transactional
    public void updateTemplate(Long id, String newTitle, TemplateRuleType newRuleType, DayOfWeek newDay) {
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 템플릿입니다. ID: " + id));

        // 🚩 템플릿의 정보만 변경합니다.
        // 기존에 이미 생성된 Task들은 template_id를 null로 끊어놨거나 그대로 갖고 있으므로 영향받지 않습니다.
        template.updateInfo(newTitle, newRuleType, newDay);
    }
}
