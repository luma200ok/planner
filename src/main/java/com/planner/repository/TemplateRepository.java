package com.planner.repository;

import com.planner.domain.Template;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TemplateRepository extends JpaRepository<Template, Long> {
    // 활성화된 반복 설정 가져오기
    List<Template> findAllByActiveTrue();
}