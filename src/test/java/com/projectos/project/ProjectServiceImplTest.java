package com.projectos.project;

import com.projectos.common.exception.BusinessRuleException;
import com.projectos.common.exception.ResourceNotFoundException;
import com.projectos.common.model.Priority;
import com.projectos.common.model.ProjectStatus;
import com.projectos.common.model.ProjectType;
import com.projectos.project.dto.CreateProjectRequest;
import com.projectos.project.dto.ProjectResponse;
import com.projectos.project.dto.UpdateProjectRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    private ProjectServiceImpl projectService;

    @BeforeEach
    void setUp() {
        projectService = new ProjectServiceImpl(projectRepository, new ProjectMapperImpl());
    }

    @Test
    void createAppliesDefaultStatusAndPriority() {
        CreateProjectRequest request = new CreateProjectRequest(
                "Learn woodworking", "desc", ProjectType.PERSONAL, null, "objective",
                null, null, null, Set.of("hobby"));

        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectResponse response = projectService.create(request);

        assertThat(response.status()).isEqualTo(ProjectStatus.PLANNING);
        assertThat(response.priority()).isEqualTo(Priority.MEDIUM);
        assertThat(response.tags()).containsExactly("hobby");
    }

    @Test
    void createHonorsExplicitPriority() {
        CreateProjectRequest request = new CreateProjectRequest(
                "Ship v1", null, ProjectType.SOFTWARE, null, null,
                null, null, Priority.CRITICAL, null);

        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectResponse response = projectService.create(request);

        assertThat(response.priority()).isEqualTo(Priority.CRITICAL);
    }

    @Test
    void createRejectsCustomTypeWhenProjectTypeIsNotOther() {
        CreateProjectRequest request = new CreateProjectRequest(
                "Name", null, ProjectType.SOFTWARE, "MyCustom", null, null, null, null, null);

        assertThatThrownBy(() -> projectService.create(request))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void createAllowsCustomTypeWhenProjectTypeIsOther() {
        CreateProjectRequest request = new CreateProjectRequest(
                "Name", null, ProjectType.OTHER, "Sabbatical", null, null, null, null, null);

        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectResponse response = projectService.create(request);

        assertThat(response.customType()).isEqualTo("Sabbatical");
    }

    @Test
    void createRejectsTargetDateBeforeStartDate() {
        CreateProjectRequest request = new CreateProjectRequest(
                "Name", null, ProjectType.SOFTWARE, null, null,
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 5, 1), null, null);

        assertThatThrownBy(() -> projectService.create(request))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void getByIdThrowsNotFoundWhenMissing() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteThrowsNotFoundWhenMissing() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateAppliesOnlyProvidedFields() {
        Project existing = new Project();
        existing.setName("Old name");
        existing.setDescription("Old description");
        existing.setProjectType(ProjectType.SOFTWARE);
        existing.setStatus(ProjectStatus.PLANNING);
        existing.setPriority(Priority.MEDIUM);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(existing));

        UpdateProjectRequest request = new UpdateProjectRequest(
                "New name", null, null, null, null, null, null, null, null);

        ProjectResponse response = projectService.update(1L, request);

        assertThat(response.name()).isEqualTo("New name");
        assertThat(response.description()).isEqualTo("Old description");
        assertThat(response.projectType()).isEqualTo(ProjectType.SOFTWARE);
    }

    @Test
    void updateRejectsTargetDateBeforeExistingStartDate() {
        Project existing = new Project();
        existing.setName("Name");
        existing.setProjectType(ProjectType.SOFTWARE);
        existing.setStatus(ProjectStatus.PLANNING);
        existing.setPriority(Priority.MEDIUM);
        existing.setStartDate(LocalDate.of(2026, 6, 1));

        when(projectRepository.findById(1L)).thenReturn(Optional.of(existing));

        UpdateProjectRequest request = new UpdateProjectRequest(
                null, null, null, null, null, null, LocalDate.of(2026, 5, 1), null, null);

        assertThatThrownBy(() -> projectService.update(1L, request))
                .isInstanceOf(BusinessRuleException.class);
    }
}
