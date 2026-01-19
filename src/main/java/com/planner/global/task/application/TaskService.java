package com.planner.global.task.application;

import com.planner.global.error.exceptiion.NotFoundException;
import com.planner.global.task.application.dto.CreateRequest;
import com.planner.global.task.application.dto.TaskResponse;
import com.planner.global.task.application.dto.UpdateRequest;
import com.planner.global.task.domain.Task;
import com.planner.global.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskResponse create(CreateRequest req) {
        Task task = new Task(req.title(), req.scheduledDate());
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
        Task task = taskRepository.findById(id).orElseThrow(() -> new NotFoundException("Task를 찾을 수 없습니다. id=" + id));
        taskRepository.delete(task);
    }
}
