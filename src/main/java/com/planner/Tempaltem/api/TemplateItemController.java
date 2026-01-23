package com.planner.Tempaltem.api;

import com.planner.Tempaltem.application.TemplateItemService;
import com.planner.Tempaltem.application.dto.TemplateItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/templates/{id}/items")
public class TemplateItemController {

    private final TemplateItemService itemService;

    @PostMapping
    public TemplateItemDto.Response add(@PathVariable Long id, @RequestBody TemplateItemDto.ItemCreateRequest req) {
        return itemService.addItem(id, req);
    }

    @GetMapping
    public List<TemplateItemDto.Response> list(@PathVariable Long id) {
        return itemService.list(id);
    }
}
