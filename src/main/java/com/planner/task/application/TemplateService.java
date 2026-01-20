package com.planner.task.application;

import com.planner.global.error.exceptiion.NotFoundException;
import com.planner.task.application.dto.TaskDto;
import com.planner.task.application.dto.TemplateDto;
import com.planner.task.domain.Task;
import com.planner.task.domain.TaskTemplate;
import com.planner.task.repository.TaskRepository;
import com.planner.task.repository.TaskTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional
@RequiredArgsConstructor
public class TemplateService {

    private final TaskRepository taskRepository;
    private final TaskTemplateRepository templateRepository;

    public TemplateDto.TemplateResponse create(TemplateDto.CreateRequest req) {
        TaskTemplate template = new TaskTemplate(req.title(), req.ruleType());
        TaskTemplate save = templateRepository.save(template);
        return TemplateDto.TemplateResponse.from(save.getId(), save.getTitle(), save.getRuleType(), save.isActive());

    }

    public TaskDto.TaskResponse generate(Long templateId, LocalDate date) {
        TaskTemplate template = templateRepository.findById(templateId).orElseThrow(
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
