package com.planner.template.api;

import com.planner.Tempaltem.application.TemplateGenerateService;
import com.planner.template.application.TemplateService;
import com.planner.Tempaltem.application.dto.GenerateResult;
import com.planner.template.application.dto.TemplateDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
    private final TemplateGenerateService templateGenerateService;

    @PostMapping
    public TemplateDto.TemplateResponse create(@RequestBody @Valid TemplateDto.TemplateCreateRequest req) {
        return templateService.create(req);
    }

    @PostMapping("/{id}/generate")
    public GenerateResult generate(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return templateGenerateService.generate(id, date);
    }


}
