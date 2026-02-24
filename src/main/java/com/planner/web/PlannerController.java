package com.planner.web;

import com.planner.application.PlannerService;
import com.planner.config.ApiStandardResponse;
import com.planner.domain.TaskStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.time.LocalDate;
import java.util.List;

import static com.planner.dto.SchedulerDto.CloseRequest;
import static com.planner.dto.TaskDto.CreateRequest;
import static com.planner.dto.TaskDto.TaskResponse;
import static com.planner.dto.TaskDto.UpdateRequest;
import static com.planner.dto.TemplateDto.TemplateCreateRequest;
import static com.planner.dto.TemplateDto.TemplateResponse;
import static com.planner.dto.TemplateDto.TemplateUpdateRequest;

@Tag(name = "Planner", description = "일정 및 템플릿 관리 API")
@RestController
@RequestMapping("/api/v1/planner")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // 프론트엔드 접속 허용
public class PlannerController {

    private final PlannerService plannerService;

    @Operation(summary = "새 할 일 생성", description = "제목과 마감 날짜를 받아 단일 할 일을 등록합니다.")
    @ApiStandardResponse
    @PostMapping("/tasks")
    public TaskResponse create(@RequestBody CreateRequest req) {
        var task = plannerService.createTask(req.title(), req.date());
        return new TaskResponse(task.getId(), task.getTitle(), task.getStatus(), task.getScheduledDate());
    }

    @Operation(summary = "할 일 수정", description = "등록된 할 일 제목을 수정합니다.")
    @ApiStandardResponse
    @PutMapping("/tasks/{id}")
    public void update(
            @Parameter(description = "수정할 할 일의 고유 번호(ID)", example = "1")
            @PathVariable Long id, @RequestBody UpdateRequest request) {
        plannerService.updateTask(id, request.title());
    }

    // 프론트엔드 보드 출력을 위한 목록 조회 API
    @Operation(summary = "할 일 목록 조회", description = "생성되어 있는 할 일 목록을 조회합니다.")
    @ApiStandardResponse
    @GetMapping("/tasks")
    public List<TaskResponse> list(
            // 🚩 value 이름을 명시하여 스프링이 파라미터를 확실히 찾게 합니다.
            @RequestParam(value = "from") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate from,
            @RequestParam(value = "to") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate to,
            @RequestParam(value = "status", required = false) TaskStatus status
    ) {
        // 🚩 서비스로 status를 넘겨줍니다.
        return plannerService.getTasks(from, to, status).stream()
                .map(t -> new TaskResponse(t.getId(), t.getTitle(), t.getStatus(), t.getScheduledDate()))
                .toList();
    }

    @ApiStandardResponse
    @Operation(summary = "할 일 삭제", description = "ID를 통해 등록된 할 일을 삭제합니다")
    @DeleteMapping("/tasks/{id}")
    public void delete(@PathVariable Long id) {
        plannerService.deleteTask(id);
    }

    @Operation(summary = "하루 마감", description = "금일 미완료 할 일을 스킵합니다.")
    @ApiStandardResponse
    @PostMapping("/day-close")
    public void close(@RequestBody CloseRequest req) {
        plannerService.closeDay(req.date(), req.carryOver());
    }

    @Operation(summary = "할 일 완료", description = "계획된 할 일을 완료 상태로 전환합니다.")
    @ApiStandardResponse
    @PostMapping("/tasks/{id}/complete")
    public void complete(@PathVariable Long id) {
        plannerService.completeTask(id);
    }

    @Operation(summary = "할 일 생략", description = "계획된 할 일을 스킵 상태로 전환합니다.")
    @ApiStandardResponse
    @PostMapping("/tasks/{id}/skip")
    public void skip(@PathVariable Long id) {
        plannerService.skipTask(id);
    }

    @Operation(summary = "계획 상태로 변경", description = "완료/생략된 할 일을 계획 상태로 전환합니다.")
    @ApiStandardResponse
    @PostMapping("/tasks/{id}/undo")
    public void undo(@PathVariable Long id) {
        plannerService.undoTask(id);
    }

    @Operation(summary = "반복 템플릿 생성", description = "매일, 평일, 주말 또는 특정 요일마다 반복되는 일정의 템플릿을 만듭니다.")
    @ApiStandardResponse
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

    @Operation(summary = "반복 템플릿 조회", description = "생성된 반복 템플릿을 조회합니다.")
    @ApiStandardResponse
    @GetMapping("/templates")
    public List<TemplateResponse> getTemplates() {
        return plannerService.getAllTemplates().stream()
                .map(t -> new TemplateResponse(t.getId(), t.getTitle(), t.getRuleType(), t.getDayOfWeek(), t.isActive()))
                .toList();
    }

    @Operation(summary = "반복 템플릿 삭제", description = "반복 템플릿을 삭제합니다. 이미 생성된 할 일은 삭제되지 않습니다.")
    @ApiStandardResponse
    @DeleteMapping("/templates/{id}")
    public void deleteTemplate(@PathVariable Long id) {
        plannerService.deleteTemplate(id);
    }

    @Operation(summary = "생성된 반복 템플릿 수정", description = "생성된 반복 템플릿 제목을 변경합니다. 기존 생성된 Task 제목은 변경되지 않습니다.")
    @ApiStandardResponse
    @PutMapping("/templates/{id}")
    public void updateTemplate(
            @Schema(description = "수정할 TemplateId",example = "1")
            @PathVariable Long id,
            @RequestBody TemplateUpdateRequest req) {
        plannerService.updateTemplate(id, req.title(), req.ruleType(), req.dayOfWeek());
    }

    // Scheduler 수동 실행
    @Operation(summary = "스케줄러를 실행 / 관리자 설정", description = "강제 스케줄러 실행 ")
    @ApiStandardResponse
    @PostMapping("/admin/run-scheduler")
    public ResponseEntity<Void> runSchedulerManual() {
        plannerService.generateWeeklyTasksFromTemplates();
        return ResponseEntity.ok().build();
    }

}
