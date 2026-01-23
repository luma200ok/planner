package com.planner.task.api;

import com.planner.task.application.TaskService;
import com.planner.task.application.dto.TaskDto.SkipRequest;
import com.planner.task.application.dto.TaskDto;
import com.planner.task.application.dto.TaskDto.UndoRequest;
import com.planner.task.application.dto.TaskDto.UpdateRequest;
import com.planner.task.domain.TaskStatus;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Tasks", description = "할일 관리 API")
@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public TaskDto.TaskResponse create(@Valid @RequestBody TaskDto.TaskCreateRequest req) {
        return taskService.create(req);
    }

    @GetMapping
    public List<TaskDto.TaskResponse> search(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) TaskStatus status
    ) {

        return taskService.search(from, to, status);
    }

    @GetMapping("/{id}")
    public TaskDto.TaskResponse get(@PathVariable Long id) {
        return taskService.get(id);
    }

    @PatchMapping("/{id}")
    public TaskDto.TaskResponse update(@PathVariable Long id, @Valid @RequestBody UpdateRequest req) {
        return taskService.update(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        taskService.delete(id);
    }

    @PostMapping("/{id}/complete")
    public TaskDto.TaskResponse complete(@PathVariable Long id,
                                         @RequestHeader("Idempotency-Key") String idempotencyKey) {
        return taskService.complete(id, idempotencyKey);
    }

    @PostMapping("/{id}/undo")
    public TaskDto.TaskResponse undo(@PathVariable Long id, @RequestBody(required = false) UndoRequest req) {
        return taskService.undo(id, req == null ? null : req.reason());
    }

    @GetMapping("/{id}/events")
    public List<TaskDto.TaskEventResponse> events(@PathVariable Long id) {
        return taskService.events(id);
    }

    @PostMapping("/{id}/skip")
    public TaskDto.TaskResponse skip(@PathVariable Long id, @RequestBody(required = false) SkipRequest req) {
        return taskService.skip(id, req == null ? null : req.reason());
    }
}
