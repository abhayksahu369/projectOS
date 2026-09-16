package com.projectos.project;

import com.projectos.common.exception.BusinessRuleException;
import com.projectos.common.exception.ResourceNotFoundException;
import com.projectos.common.model.Priority;
import com.projectos.common.model.ProjectStatus;
import com.projectos.common.model.ProjectType;
import com.projectos.project.dto.CreateProjectRequest;
import com.projectos.project.dto.ProjectResponse;
import com.projectos.project.dto.UpdateProjectRequest;
import com.projectos.project.dto.UpdateProjectStatusRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;

@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    public ProjectServiceImpl(ProjectRepository projectRepository, ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
    }

    @Override
    public ProjectResponse create(CreateProjectRequest request) {
        validateCustomType(request.projectType(), request.customType());
        validateDateOrder(request.startDate(), request.targetDate());

        Project project = projectMapper.toEntity(request);
        project.setStatus(ProjectStatus.PLANNING);
        project.setPriority(request.priority() != null ? request.priority() : Priority.MEDIUM);
        project.setTags(request.tags() != null ? new HashSet<>(request.tags()) : new HashSet<>());

        Project saved = projectRepository.save(project);
        return projectMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponse> list(Pageable pageable) {
        return projectRepository.findAll(pageable).map(projectMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getById(Long id) {
        return projectMapper.toResponse(requireProject(id));
    }

    @Override
    public ProjectResponse update(Long id, UpdateProjectRequest request) {
        Project project = requireProject(id);

        ProjectType effectiveType = request.projectType() != null ? request.projectType() : project.getProjectType();
        String effectiveCustomType = request.customType() != null ? request.customType() : project.getCustomType();
        validateCustomType(effectiveType, effectiveCustomType);

        LocalDate effectiveStart = request.startDate() != null ? request.startDate() : project.getStartDate();
        LocalDate effectiveTarget = request.targetDate() != null ? request.targetDate() : project.getTargetDate();
        validateDateOrder(effectiveStart, effectiveTarget);

        projectMapper.updateEntityFromRequest(request, project);
        if (request.tags() != null) {
            project.setTags(new HashSet<>(request.tags()));
        }

        return projectMapper.toResponse(project);
    }

    @Override
    public ProjectResponse updateStatus(Long id, UpdateProjectStatusRequest request) {
        Project project = requireProject(id);
        project.setStatus(request.status());
        return projectMapper.toResponse(project);
    }

    @Override
    public void delete(Long id) {
        Project project = requireProject(id);
        projectRepository.delete(project);
    }

    Project requireProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Project", id));
    }

    private void validateCustomType(ProjectType projectType, String customType) {
        if (customType != null && !customType.isBlank() && projectType != ProjectType.OTHER) {
            throw new BusinessRuleException("customType is only accepted when projectType is OTHER");
        }
    }

    private void validateDateOrder(LocalDate startDate, LocalDate targetDate) {
        if (startDate != null && targetDate != null && targetDate.isBefore(startDate)) {
            throw new BusinessRuleException("targetDate must not be before startDate");
        }
    }
}
