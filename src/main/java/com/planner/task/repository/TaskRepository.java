package com.planner.task.repository;

import com.planner.report.application.dto.DailyReportRow;
import com.planner.report.application.dto.SummaryRow;
import com.planner.task.domain.Task;
import com.planner.task.domain.TaskStatus;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    @Query("select new com.planner.report.application.dto.DailyReportRow(" +
            "t.scheduledDate, count(t)," +
            " sum(case when t.status = com.planner.task.domain.TaskStatus.DONE then 1 else 0 end)," +
            " sum(case when t.status = com.planner.task.domain.TaskStatus.SKIPPED then 1 else 0 end )," +
            " sum(case when t.status = com.planner.task.domain.TaskStatus.PLANNED then 1 else 0 end))" +
            " from Task t" +
            " where t.scheduledDate between :from and :to" +
            " group by t.scheduledDate" +
            " order by t.scheduledDate asc")
    List<DailyReportRow> dailyReport(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("select new com.planner.report.application.dto.SummaryRow(" +
            "count(t)," +
            " sum(case when t.status = com.planner.task.domain.TaskStatus.DONE then 1 else 0 end)," +
            " sum(case when t.status = com.planner.task.domain.TaskStatus.SKIPPED then 1 else 0 end )," +
            " sum(case when t.status = com.planner.task.domain.TaskStatus.PLANNED then 1 else 0 end))" +
            " from Task t" +
            " where t.scheduledDate between :from and :to")
    public SummaryRow summary(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
