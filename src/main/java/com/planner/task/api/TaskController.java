package com.planner.task.api;

import com.planner.task.application.TaskService;
import com.planner.task.application.dto.CreateRequest;
import com.planner.task.application.dto.TaskEventResponse;
import com.planner.task.application.dto.TaskResponse;
import com.planner.task.application.dto.UpdateRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
    public TaskResponse create(@Valid @RequestBody CreateRequest req) {
        return taskService.create(req);
    }

    @GetMapping
    public List<TaskResponse> list(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {
        return taskService.findByDate(date);
    }

    @GetMapping("/{id}")
    public TaskResponse get(@PathVariable Long id) {
        return taskService.get(id);
    }

    @PatchMapping("/{id}")
    public TaskResponse update(@PathVariable Long id, @Valid @RequestBody UpdateRequest req) {
        return taskService.update(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        taskService.delete(id);
    }

    @PostMapping("/{id}/complete")
    public TaskResponse complete(@PathVariable Long id, @RequestHeader("Idempotency-Key") String idempotencyKey) {
        return taskService.complete(id, idempotencyKey);
    }

    @PostMapping("/{id}/undo")
    public TaskResponse undo(@PathVariable Long id) {
        return taskService.undo(id);
    }

    @GetMapping("/{id}/events")
    public List<TaskEventResponse> events(@PathVariable Long id) {
        return taskService.events(id);
    }
}
