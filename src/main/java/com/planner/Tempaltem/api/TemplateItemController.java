package com.planner.Tempaltem.api;

import com.planner.Tempaltem.application.TemplateItemService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.planner.Tempaltem.application.dto.TemplateItemDto.*;

@Tag(name = "Template Items", description = "템플릿 할일 목록 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/templates/{id}/items")
public class TemplateItemController {

    private final TemplateItemService itemService;

    @PostMapping
    public ItemResponse add(@PathVariable Long id, @RequestBody ItemCreateRequest req) {
        return itemService.addItem(id, req);
    }

    @GetMapping
    public List<ItemResponse> list(@PathVariable Long id) {
        return itemService.list(id);
    }
}
