package com.planner.Tempaltem.application.dto;

import com.planner.Tempaltem.domain.TemplateItem;

public class TemplateItemDto {
    public record ItemCreateRequest(String title, Integer sortOrder) {}

    public record ItemResponse(Long id, String title, int sortOrder) {
        public static ItemResponse from(TemplateItem item) {
            return new ItemResponse(item.getId(), item.getName(), item.getSortOrder());
        }
    }
}
