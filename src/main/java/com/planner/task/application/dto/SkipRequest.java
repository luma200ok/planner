package com.planner.task.application.dto;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public record SkipRequest(String reason) {}
