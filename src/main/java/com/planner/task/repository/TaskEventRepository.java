package com.planner.task.repository;

import com.planner.task.event.TaskEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskEventRepository extends JpaRepository<TaskEvent, Long> {
    List<TaskEvent> findByTaskIdOrderByOccurredAtDesc(Long taskId);

    Optional<TaskEvent> findByIdempotencyKey(String idempotencyKey);
}
