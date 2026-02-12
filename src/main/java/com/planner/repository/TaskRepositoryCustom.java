package com.planner.repository;

import com.planner.domain.Task;
import com.planner.domain.TaskStatus;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepositoryCustom {
    // 동적 필터링을 위한 검색 메서드
    List<Task> searchTasks(LocalDate from, LocalDate to, TaskStatus status, String keyword);
}
