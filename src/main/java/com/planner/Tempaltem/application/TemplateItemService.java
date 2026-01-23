package com.planner.Tempaltem.application;

import com.planner.Tempaltem.application.dto.TemplateItemDto;
import com.planner.template.domain.Template;
import com.planner.Tempaltem.domain.TemplateItem;
import com.planner.Tempaltem.repository.TemplateItemRepository;
import com.planner.template.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.planner.Tempaltem.application.dto.TemplateItemDto.*;

@Service
@RequiredArgsConstructor
public class TemplateItemService {

    private final TemplateRepository templateRepository;
    private final TemplateItemRepository itemRepository;

    @Transactional
    public ItemResponse addItem(Long templateId, TemplateItemDto.ItemCreateRequest req) {
        Template template = templateRepository.findById(templateId).orElseThrow(
                () -> new IllegalArgumentException("template not found: " + templateId));

        int sortOrder = (req.sortOrder() == null) ? 0 : req.sortOrder();
        TemplateItem item = new TemplateItem(template, req.title(), req.sortOrder());

        return ItemResponse.from(itemRepository.save(item));
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> list(Long templateId) {
        return itemRepository.findAllByTemplateIdOrderBySortOrderAscIdAsc(templateId)
                .stream().map(ItemResponse::from).toList();
    }
}
