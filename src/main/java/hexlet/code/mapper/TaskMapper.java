package hexlet.code.mapper;

import hexlet.code.dto.TaskDTO.TaskCreateDTO;
import hexlet.code.dto.TaskDTO.TaskDTO;
import hexlet.code.dto.TaskDTO.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskStatusRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mapper(
        uses = {JsonNullableMapper.class, ReferenceMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class TaskMapper {

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Mapping(source = "title", target = "name")
    @Mapping(source = "content", target = "description")
    @Mapping(target = "taskStatus.slug", ignore = true)
    @Mapping(target = "assignee.id", ignore = true)
    @Mapping(target = "labels", ignore = true)
    public abstract Task map(TaskCreateDTO taskCreateDTO);

    @Mapping(source = "assignee.id", target = "assigneeId")
    @Mapping(source = "description", target = "content")
    @Mapping(source = "name", target = "title")
    @Mapping(source = "taskStatus.slug", target = "status")
    @Mapping(source = "labels", target = "taskLabelIds")
    public abstract TaskDTO map(Task task);

    @Mapping(source = "title", target = "name")
    @Mapping(source = "content", target = "description")
    @Mapping(target = "taskStatus.slug", ignore = true)
    @Mapping(target = "assignee.id", ignore = true)
    @Mapping(target = "labels", ignore = true)
    public abstract void update(TaskUpdateDTO data, @MappingTarget Task model);

    public TaskStatus toEntity(String slug) {
        return taskStatusRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Status not found"));
    }

    public List<Long> toIds(Set<Label> labels) {
        return labels == null
                ? null
                : labels.stream()
                .map(Label::getId)
                .toList();
    }

    public Set<Label> toLabelSet(List<Long> taskLabelIds) {
        return new HashSet<>(labelRepository.findByIdIn(taskLabelIds).orElse(new HashSet<>()));
    }

}
