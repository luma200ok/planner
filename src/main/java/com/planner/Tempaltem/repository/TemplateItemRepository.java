package com.planner.Tempaltem.repository;

import com.planner.Tempaltem.domain.TemplateItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TemplateItemRepository extends JpaRepository<TemplateItem, Long> {
    List<TemplateItem> findAllByTemplateIdOrderBySortOrderAscIdAsc(Long templateId);
}

