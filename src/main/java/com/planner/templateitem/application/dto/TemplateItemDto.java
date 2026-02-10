package com.planner.templateitem.application.dto;

import com.planner.templateitem.domain.TemplateItem;
import com.planner.template.domain.TemplateRuleType;

import java.time.DayOfWeek;

public class TemplateItemDto {

    public record ItemCreateRequest(
            String title,
            TemplateRuleType ruleType,
            DayOfWeek dayOfWeek,
            Integer sortOrder
    ) {}

    public record ItemResponse(
            Long id,
            String title,
            TemplateRuleType ruleType,
            DayOfWeek dayOfWeek,
            int sortOrder
    ) {
        public static ItemResponse from(TemplateItem item) {
            return new ItemResponse(
                    item.getId(),
                    item.getName(),
                    item.getRuleType(),
                    item.getDayOfWeek(),
                    item.getSortOrder()
            );
        }
    }
}
