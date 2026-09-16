package com.projectos.project;

import com.projectos.project.dto.CreateProjectRequest;
import com.projectos.project.dto.ProjectResponse;
import com.projectos.project.dto.UpdateProjectRequest;
import com.projectos.project.dto.UpdateProjectStatusRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectService {

    ProjectResponse create(CreateProjectRequest request);

    Page<ProjectResponse> list(Pageable pageable);

    ProjectResponse getById(Long id);

    ProjectResponse update(Long id, UpdateProjectRequest request);

    ProjectResponse updateStatus(Long id, UpdateProjectStatusRequest request);

    void delete(Long id);
}
