package com.planner.web;

import com.planner.application.PlannerService;
import com.planner.domain.TaskStatus;
import com.planner.domain.TemplateRuleType;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/planner")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // 프론트엔드 접속 허용
public class PlannerController {

    private final PlannerService plannerService;

    public record TaskResponse(Long id, String title, TaskStatus status, LocalDate date) {}
    public record CloseRequest(LocalDate date, boolean carryOver) {}

    public record CreateRequest(String title, LocalDate date) {}
    public record UpdateRequest(String title) {}
    public record TemplateRequest(String title, TemplateRuleType ruleType, DayOfWeek dayOfWeek,LocalDate selectedDate) {}
    public record TemplateCreateRequest(
            String title,
            TemplateRuleType ruleType,
            DayOfWeek dayOfWeek,
            LocalDate date // 🚩 프론트엔드의 "2026-02-19"가 이 필드로 들어옵니다.
    ) {}

    @PostMapping("/tasks")
    public TaskResponse create(@RequestBody CreateRequest req) {
        var task = plannerService.createTask(req.title(), req.date());
        return new TaskResponse(task.getId(), task.getTitle(), task.getStatus(), task.getScheduledDate());
    }

    @PutMapping("/tasks/{id}")
    public void update(@PathVariable Long id, @RequestBody UpdateRequest request) {
        plannerService.updateTask(id, request.title());
    }

    // 프론트엔드 보드 출력을 위한 목록 조회 API
    @GetMapping("/tasks")
    public List<TaskResponse> list(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return plannerService.getTasks(from, to).stream()
                .map(t -> new TaskResponse(t.getId(), t.getTitle(), t.getStatus(), t.getScheduledDate()))
                .toList();
    }

    @DeleteMapping("/tasks/{id}")
    public void delete(@PathVariable Long id) {
        plannerService.deleteTask(id);
    }

    @PostMapping("/tasks/{id}/complete")
    public void complete(@PathVariable Long id) {
        plannerService.completeTask(id);
    }

    @PostMapping("/day-close")
    public void close(@RequestBody CloseRequest req) {
        plannerService.closeDay(req.date(), req.carryOver());
    }

    @PostMapping("/templates")
    public ResponseEntity<Void> createTemplate(@RequestBody TemplateCreateRequest request) {
        // 🚩 request.date()를 통해 프론트의 날짜를 서비스로 명확히 전달합니다.
        plannerService.createTemplate(
                request.title(),
                request.ruleType(),
                request.dayOfWeek(),
                request.date()
        );
        return ResponseEntity.ok().build();
    }
}
