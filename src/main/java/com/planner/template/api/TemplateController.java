package com.planner.template.api;

import com.planner.templateitem.application.TemplateGenerateService;
import com.planner.template.application.TemplateService;
import com.planner.templateitem.application.dto.GenerateResult;
import com.planner.template.application.dto.TemplateDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Template", description = "반복 템플릿 API")
@RestController
@RequestMapping("/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;
    private final TemplateGenerateService templateGenerateService;

    @PostMapping
    public TemplateDto.TemplateResponse create(@RequestBody @Valid TemplateDto.TemplateCreateRequest req) {
        return templateService.create(req);
    }

    @PostMapping("/{id}/generate")
    public GenerateResult generate(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestParam(required = false) String title
    ) {
        return templateGenerateService.generate(id, date,title);
    }

    @GetMapping
    public List<TemplateDto.TemplateResponse> list() {
        return templateService.list();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        templateService.delete(id);
    }
}
