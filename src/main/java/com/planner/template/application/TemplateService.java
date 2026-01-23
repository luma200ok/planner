package com.planner.template.application;

import com.planner.global.error.exceptiion.NotFoundException;
import com.planner.task.application.dto.TaskDto;
import com.planner.task.domain.Task;
import com.planner.template.domain.Template;
import com.planner.task.repository.TaskRepository;
import com.planner.template.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static com.planner.template.application.dto.TemplateDto.*;

@Service
@Transactional
@RequiredArgsConstructor
public class TemplateService {

    private final TaskRepository taskRepository;
    private final TemplateRepository templateRepository;

    public TemplateResponse create(TemplateCreateRequest req) {
        Template template = new Template(req.title(), req.ruleType());
        Template save = templateRepository.save(template);
        return TemplateResponse.from(save.getId(), save.getTitle(), save.getRuleType(), save.isActive());

    }

    public TaskDto.TaskResponse generate(Long templateId, LocalDate date) {
        Template template = templateRepository.findById(templateId).orElseThrow(
                () -> new NotFoundException("template not found : " + templateId));

        if (!template.isActive() || !template.matches(date)) {
            throw new IllegalStateException("template does not match date");
        }

        taskRepository.findByTemplateIdAndScheduledDate(templateId, date).ifPresent(t -> {
            throw new IllegalStateException("already generated");
        });

        Task task = new Task(template.getTitle(), date);
        task.attachTemplate(template);
        taskRepository.save(task);

        return TaskDto.TaskResponse.from(task);
    }
}
