package com.planner.templateitem.application;

import com.planner.task.domain.Task;
import com.planner.task.repository.TaskRepository;
import com.planner.templateitem.application.dto.GenerateResult;
import com.planner.template.domain.Template;
import com.planner.templateitem.domain.TemplateItem;
import com.planner.template.domain.TemplateRuleType;
import com.planner.template.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TemplateGenerateService {

    private final TemplateRepository templateRepository;
    private final TaskRepository taskRepository;

    @Transactional
    public GenerateResult generate(Long templateId, LocalDate baseDate,  String title ) {

        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("template not found"));

        List<TemplateItem> items = template.getItems();

        List<Task> created = new ArrayList<>();

        LocalDate monday = baseDate.with(DayOfWeek.MONDAY);

        for (TemplateItem item : items) {

                if (item.getRuleType() == TemplateRuleType.DAILY) {
                for (int i = 0; i < 7; i++) {
                    LocalDate date = monday.plusDays(i);
                    created.add(taskRepository.save(
                            Task.create(item.getName(), date)
                    ));
                }
            }

            if (item.getRuleType() == TemplateRuleType.WEEKLY) {
                LocalDate date = monday.with(item.getDayOfWeek());
                created.add(taskRepository.save(
                        Task.create(item.getName(), date)
                ));
            }
        }

        return new GenerateResult(created.size(),0);
    }
}
