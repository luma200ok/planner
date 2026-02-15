package com.planner.repository;

import com.planner.domain.Task;
import com.planner.domain.TaskStatus;
import com.planner.domain.Template;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long>, TaskRepositoryCustom {
    List<Task> findAllByScheduledDateAndStatus(LocalDate date, TaskStatus status);
    Optional<Task> findByTemplateAndScheduledDate(Template template, LocalDate date);
}