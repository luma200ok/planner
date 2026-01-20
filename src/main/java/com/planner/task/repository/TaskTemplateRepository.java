package com.planner.task.repository;

import com.planner.task.domain.TaskTemplate;
import org.springframework.data.jpa.repository.JpaRepository;


public interface TaskTemplateRepository extends JpaRepository<TaskTemplate,Long> {

}