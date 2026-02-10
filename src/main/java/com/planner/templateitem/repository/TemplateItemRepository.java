package com.planner.templateitem.repository;

import com.planner.templateitem.domain.TemplateItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TemplateItemRepository extends JpaRepository<TemplateItem, Long> {
    List<TemplateItem> findAllByTemplateIdOrderBySortOrderAscIdAsc(Long templateId);
}

