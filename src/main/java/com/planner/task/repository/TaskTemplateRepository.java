package com.planner.task.repository;

import com.planner.task.domain.TaskTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface TaskTemplateRepository extends JpaRepository<TaskTemplate,Long> {

    List<TaskTemplate> findAllByActiveTrue();
}