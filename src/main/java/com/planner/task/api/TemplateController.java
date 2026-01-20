package com.planner.task.api;

import com.planner.task.application.TemplateService;
import com.planner.task.application.dto.TaskDto;
import com.planner.task.application.dto.TemplateDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "Templates")
@RestController
@RequestMapping("/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @PostMapping
    public TemplateDto.TemplateResponse create(@RequestBody @Valid TemplateDto.CreateRequest req) {
        return templateService.create(req);
    }

    @PostMapping("/{id}/generate")
    public TaskDto.TaskResponse generate(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return templateService.generate(id, date);
    }
}
