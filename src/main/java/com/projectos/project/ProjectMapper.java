package com.projectos.project;

import com.projectos.project.dto.CreateProjectRequest;
import com.projectos.project.dto.ProjectResponse;
import com.projectos.project.dto.UpdateProjectRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProjectMapper {

    ProjectResponse toResponse(Project project);

    @Mapping(target = "tags", ignore = true)
    Project toEntity(CreateProjectRequest request);

    @Mapping(target = "tags", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(UpdateProjectRequest request, @MappingTarget Project project);
}
