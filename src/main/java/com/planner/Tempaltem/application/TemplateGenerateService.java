package com.planner.Tempaltem.application;

import com.planner.task.domain.Task;
import com.planner.task.repository.TaskRepository;
import com.planner.Tempaltem.application.dto.GenerateResult;
import com.planner.template.domain.Template;
import com.planner.Tempaltem.domain.TemplateItem;
import com.planner.template.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TemplateGenerateService {

    private final TemplateRepository templateRepository;
    private final TaskRepository taskRepository;

    @Transactional
    public GenerateResult generate(Long templateId, LocalDate date) {
        Template template = templateRepository.findByIdWithItems(templateId)
                .orElseThrow(() -> new IllegalArgumentException("template not found: " + templateId));

        int generated = 0;
        int duplicated = 0;

        for (TemplateItem item : template.getItems()) {
            Task task = Task.fromTemplateItem(template, item, date);
            try {
                taskRepository.save(task);
                generated++;
            } catch (DataIntegrityViolationException e) {
                duplicated++;
            }
        }
        return new GenerateResult(generated, duplicated);
    }
}
