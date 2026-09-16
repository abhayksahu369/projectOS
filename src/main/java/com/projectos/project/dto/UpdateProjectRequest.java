package com.projectos.project.dto;

import com.projectos.common.model.Priority;
import com.projectos.common.model.ProjectType;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

public record UpdateProjectRequest(
        @Size(max = 255) String name,
        @Size(max = 2000) String description,
        ProjectType projectType,
        @Size(max = 100) String customType,
        @Size(max = 2000) String objective,
        LocalDate startDate,
        LocalDate targetDate,
        Priority priority,
        Set<@Size(max = 100) String> tags
) {
}
