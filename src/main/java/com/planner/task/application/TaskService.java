package com.planner.task.application;

import com.planner.global.error.exceptiion.NotFoundException;
import com.planner.task.application.dto.TaskDto;
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


    public TaskDto.TaskResponse create(TaskDto.CreateRequest req) {
        LocalDate date = req.scheduledDate();
        if (date == null) {
            date = LocalDate.now(ZoneId.of("Asia/Seoul"));
        }
        Task task = new Task(req.title(), date);
        return TaskDto.TaskResponse.from(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public List<TaskDto.TaskResponse> search(LocalDate from, LocalDate to, TaskStatus status) {
        return taskRepository.search(from, to, status).stream().map(TaskDto.TaskResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public TaskDto.TaskResponse get(Long id) {
        Task task = taskRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Task not found :" + id));
        return TaskDto.TaskResponse.from(task);
    }

    public TaskDto.TaskResponse update(Long id, TaskDto.UpdateRequest req) {
        Task task = taskRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Task not found :" + id));
        task.update(req.title(), req.scheduledDate());
        return TaskDto.TaskResponse.from(task);
    }

    public void delete(Long id) {
        Task task = taskRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Task not found :" + id));
        taskRepository.delete(task);
    }

    public TaskDto.TaskResponse complete(Long id, String idempotencyKey) {
        TaskEvent existed = taskEventRepository.findByIdempotencyKey(idempotencyKey).orElse(null);

        Task task = taskRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Task not found :" + id));

        if (existed != null) {
            return TaskDto.TaskResponse.from(task); // 이미 처리된 요청 -> 현재 task로 반환
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
            return TaskDto.TaskResponse.from(task);
        }
        return TaskDto.TaskResponse.from(task);
    }

    public TaskDto.TaskResponse undo(Long id, String reason) {
        Task task = taskRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Task not found :" + id));

        if (!task.isDone()) {
            return TaskDto.TaskResponse.from(task);
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

        return TaskDto.TaskResponse.from(task);
    }

    @Transactional(readOnly = true)
    public List<TaskDto.TaskEventResponse> events(Long taskId) {
        return taskEventRepository.findByTaskIdOrderByOccurredAtDesc(taskId).stream()
                .map(TaskDto.TaskEventResponse::from).toList();
    }

    public TaskDto.TaskResponse skip(Long id, String reason) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new NotFoundException("Task not found :" + id));
        if (task.getStatus() == TaskStatus.DONE || task.getStatus() == TaskStatus.SKIPPED) {
            return TaskDto.TaskResponse.from(task);
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
        return TaskDto.TaskResponse.from(task);
    }
}
