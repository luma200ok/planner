package com.planner.templateitem.application;

import com.planner.templateitem.application.dto.TemplateItemDto;
import com.planner.template.domain.Template;
import com.planner.templateitem.domain.TemplateItem;
import com.planner.templateitem.repository.TemplateItemRepository;
import com.planner.template.domain.TemplateRuleType;
import com.planner.template.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.planner.templateitem.application.dto.TemplateItemDto.*;

@Service
@RequiredArgsConstructor
public class TemplateItemService {

    private final TemplateRepository templateRepository;
    private final TemplateItemRepository itemRepository;

    @Transactional
    public TemplateItemDto.ItemResponse addItem(
            Long templateId,
            TemplateItemDto.ItemCreateRequest req
    ) {
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() ->
                        new IllegalArgumentException("template not found: " + templateId));

        int sortOrder = (req.sortOrder() == null) ? 0 : req.sortOrder();

        if (req.ruleType() == TemplateRuleType.WEEKLY && req.dayOfWeek() == null) {
            throw new IllegalArgumentException("WEEKLY 타입은 dayOfWeek가 필요합니다");
        }

        TemplateItem item = new TemplateItem(template, req.title(), sortOrder);
        item.setRuleType(req.ruleType());

        if (req.ruleType() == TemplateRuleType.WEEKLY) {
            item.setDayOfWeek(req.dayOfWeek());
        } else {
            item.setDayOfWeek(null);
        }

        return TemplateItemDto.ItemResponse.from(itemRepository.save(item));
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> list(Long templateId) {
        return itemRepository.findAllByTemplateIdOrderBySortOrderAscIdAsc(templateId)
                .stream().map(ItemResponse::from).toList();
    }
}
