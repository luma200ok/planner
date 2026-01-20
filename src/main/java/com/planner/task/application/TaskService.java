package com.planner.task.application;

import com.planner.global.error.exceptiion.NotFoundException;
import com.planner.task.application.dto.CreateRequest;
import com.planner.task.application.dto.TaskEventResponse;
import com.planner.task.application.dto.TaskResponse;
import com.planner.task.application.dto.UpdateRequest;
import com.planner.task.domain.Task;
import com.planner.task.domain.TaskStatus;
import com.planner.task.event.TaskEvent;
import com.planner.task.event.TaskEventType;
import com.planner.task.repository.TaskEventRepository;
import com.planner.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskEventRepository taskEventRepository;

    public TaskResponse create(CreateRequest req) {
        LocalDate date = req.scheduledDate();
        if (date == null) {
            date = LocalDate.now(ZoneId.of("Asia/Seoul"));
        }
        Task task = new Task(req.title(), date);
        Task saved = taskRepository.save(task);
        return TaskResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> findByDate(LocalDate date) {
        return taskRepository.findAllByScheduledDateOrderByIdDesc(date)
                .stream().map(TaskResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse get(Long id) {
        Task task = taskRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Task를 찾을 수 없습니다. id=" + id));
        return TaskResponse.from(task);
    }

    public TaskResponse update(Long id, UpdateRequest req) {
        Task task = taskRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Task를 찾을 수 없습니다. id=" + id));
        task.update(req.title(), req.scheduledDate());
        return TaskResponse.from(task);
    }

    public void delete(Long id) {
        Task task = taskRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Task를 찾을 수 없습니다. id=" + id));
        taskRepository.delete(task);
    }

    public TaskResponse complete(Long id, String idempotencyKey) {
        TaskEvent existed = taskEventRepository.findByIdempotencyKey(idempotencyKey).orElse(null);

        Task task = taskRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Task를 찾을 수 없습니다. id=" + id));

        if (existed != null) {
            return TaskResponse.from(task); // 이미 처리된 요청 -> 현재 task로 반환
        }

        // 처리 로직
        var now = LocalDateTime.now();
        task.complete(now);
        try {
            taskEventRepository.save(TaskEvent.of(
                    task.getId(),
                    TaskEventType.COMPLETE,
                    now,
                    idempotencyKey,
                    null
            ));
        } catch (DataIntegrityViolationException e) {
            return TaskResponse.from(task);
        }
        return TaskResponse.from(task);
    }

    public TaskResponse undo(Long id, String reason) {
        Task task = taskRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Task를 찾을 수 없습니다. id=" + id));

        if (!task.isDone()) {
            return TaskResponse.from(task);
        }

        var now = LocalDateTime.now();
        task.undo();

        taskEventRepository.save(TaskEvent.of(
                task.getId(),
                TaskEventType.UNDO,
                now,
                null,
                reason
        ));

        return TaskResponse.from(task);
    }

    @Transactional(readOnly = true)
    public List<TaskEventResponse> events(Long taskId) {
        return taskEventRepository.findByTaskIdOrderByOccurredAtDesc(taskId).stream()
                .map(TaskEventResponse::from).toList();
    }

    public TaskResponse skip(Long id, String reason) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new NotFoundException("task가 없습니다. id = " + id));
        if (task.getStatus() == TaskStatus.DONE || task.getStatus() == TaskStatus.SKIPPED) {
            return TaskResponse.from(task);
        }

        var now = LocalDateTime.now();
        task.skip(now);

        taskEventRepository.save(TaskEvent.of(
                task.getId(),
                TaskEventType.SKIP,
                now,
                null,
                reason
        ));
        return TaskResponse.from(task);
    }
}
