package com.planner.task.application;

import com.planner.global.error.exceptiion.NotFoundException;
import com.planner.task.application.dto.CreateRequest;
import com.planner.task.application.dto.TaskEventResponse;
import com.planner.task.application.dto.TaskResponse;
import com.planner.task.application.dto.UpdateRequest;
import com.planner.task.domain.Task;
import com.planner.task.event.TaskEvent;
import com.planner.task.event.TaskEventType;
import com.planner.task.repository.TaskEventRepository;
import com.planner.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
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

    public TaskResponse complete(Long id) {
        Task task = taskRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Task를 찾을 수 없습니다. id=" + id));
        var now = LocalDateTime.now();
        task.complete(now);

        taskEventRepository.save(TaskEvent.of(
                task.getId(),
                TaskEventType.COMPLETE,
                now,
                null,
                null
        ));

        return TaskResponse.from(task);
    }

    public TaskResponse undo(Long id) {
        Task task = taskRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Task를 찾을 수 없습니다. id=" + id));
        var now = LocalDateTime.now();
        task.undo();

        taskEventRepository.save(TaskEvent.of(
                task.getId(),
                TaskEventType.UNDO,
                now,
                null,
                null
        ));

        return TaskResponse.from(task);
    }

    @Transactional(readOnly = true)
    public List<TaskEventResponse> events(Long taskId) {
        return taskEventRepository.findByTaskIdOrderByOccurredAtDesc(taskId).stream()
                .map(TaskEventResponse::from).toList();
    }
}
