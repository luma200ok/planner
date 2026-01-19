package com.planner.global.task.application;

import com.planner.global.task.application.dto.CreateRequest;
import com.planner.global.task.application.dto.TaskResponse;
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
}
