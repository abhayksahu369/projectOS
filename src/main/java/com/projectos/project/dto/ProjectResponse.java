package com.projectos.project.dto;

import com.projectos.common.model.Priority;
import com.projectos.common.model.ProjectStatus;
import com.projectos.common.model.ProjectType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

public record ProjectResponse(
        Long id,
        String name,
        String description,
        ProjectType projectType,
        String customType,
        String objective,
        LocalDate startDate,
        LocalDate targetDate,
        ProjectStatus status,
        Priority priority,
        Set<String> tags,
        Instant createdAt,
        Instant updatedAt
) {
}
