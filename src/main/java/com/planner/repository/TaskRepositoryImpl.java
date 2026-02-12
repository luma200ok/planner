package com.planner.repository;

import com.planner.domain.QTask;
import com.planner.domain.Task;
import com.planner.domain.TaskStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
public class TaskRepositoryImpl implements TaskRepositoryCustom{

    private final JPAQueryFactory queryFactory;
    private final QTask task = QTask.task;

    @Override
    public List<Task> searchTasks(LocalDate from, LocalDate to, TaskStatus status, String keyword) {
        return queryFactory.select(task)
                .where(
                        task.scheduledDate.between(from, to),
                        statusEq(status),
                        titleContains(keyword)
                )
                .fetch();
    }
    // 조건부 쿼리를 위한 BooleanExpression 메서드들
    private BooleanExpression statusEq(TaskStatus status) {
        return status != null ? task.status.eq(status) : null;
    }

    private BooleanExpression titleContains(String keyword) {
        return StringUtils.hasText(keyword) ? task.title.contains(keyword) : null;
    }

}
