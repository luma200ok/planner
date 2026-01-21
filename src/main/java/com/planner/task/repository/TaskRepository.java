package com.planner.task.repository;

import com.planner.task.domain.Task;
import com.planner.task.domain.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByScheduledDateOrderByIdDesc(LocalDate scheduledDate);

    @Query(
            "select t from Task t" +
            " where (:from is null or t.scheduledDate >= :from)" +
            " and(:to is null or t.scheduledDate <= :to)" +
            " and(:status is null or t.status = :status)" +
            " order by t.scheduledDate asc, t.id asc")
    List<Task> search(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("status") TaskStatus status
    );

    Optional<Task> findByTemplateIdAndScheduledDate(Long template_id, LocalDate scheduledDate);

    List<Task> findAllByScheduledDateAndStatus(LocalDate scheduledDate, TaskStatus status);
}
