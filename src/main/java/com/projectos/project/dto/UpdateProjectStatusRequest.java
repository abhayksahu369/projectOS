package com.projectos.project.dto;

import com.projectos.common.model.ProjectStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateProjectStatusRequest(
        @NotNull ProjectStatus status
) {
}
