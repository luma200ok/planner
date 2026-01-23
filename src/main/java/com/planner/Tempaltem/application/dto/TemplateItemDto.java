package com.planner.Tempaltem.application.dto;

import com.planner.Tempaltem.domain.TemplateItem;

public class TemplateItemDto {
    public record ItemCreateRequest(String name, Integer sortOrder) {}

    public record Response(Long id, String name, int sortOrder) {
        public static Response from(TemplateItem item) {
            return new Response(item.getId(), item.getName(), item.getSortOrder());
        }
    }
}
