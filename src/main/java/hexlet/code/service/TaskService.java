package hexlet.code.service;

import hexlet.code.dto.TaskDTO.TaskCreateDTO;
import hexlet.code.dto.TaskDTO.TaskDTO;
import hexlet.code.dto.TaskDTO.TaskParamsDTO;
import hexlet.code.dto.TaskDTO.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Label;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.specification.TaskSpecification;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class TaskService {

    private TaskRepository taskRepository;
    private TaskMapper taskMapper;
    private TaskStatusRepository taskStatusRepository;
    private UserRepository userRepository;
    private LabelRepository labelRepository;
    private TaskSpecification specBuilder;

    public List<TaskDTO> getAllTasks(TaskParamsDTO paramsDTO) {
        var specification = specBuilder.build(paramsDTO);
        return taskRepository.findAll(specification).stream()
                .map(taskMapper::map)
                .toList();
    }

    public TaskDTO createTask(TaskCreateDTO dto) {
        var task = taskMapper.map(dto);

        User assignee = null;
        if (dto.getAssigneeId() != null) {
            assignee = userRepository.findById(dto.getAssigneeId()).orElse(null);
        }
        task.setAssignee(assignee);

        TaskStatus taskStatus = null;
        if (dto.getStatus() != null) {
            taskStatus = taskStatusRepository.findBySlug(dto.getStatus()).orElse(null);
        }
        task.setTaskStatus(taskStatus);

        Set<Label> labelSet = null;
        if (dto.getTaskLabelIds() != null) {
            labelSet = labelRepository.findByIdIn((dto.getTaskLabelIds())).orElse(null);
        }
        task.setLabels(labelSet);

        taskRepository.save(task);
        return taskMapper.map(task);
    }

    public TaskDTO findById(Long taskId) {
        var task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id: " + taskId + " not found."));
        return taskMapper.map(task);
    }

    public TaskDTO updateTask(Long taskId, TaskUpdateDTO data) {
        var task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id: " + taskId + " not found."));

        taskMapper.update(data, task);

        User assignee = null;
        if (data.getAssigneeId() != null) {
            assignee = userRepository.findById(data.getAssigneeId().get()).orElse(null);
        }
        task.setAssignee(assignee);

        TaskStatus taskStatus = null;
        if (data.getStatus() != null) {
            taskStatus = taskStatusRepository.findBySlug(data.getStatus().get()).orElse(null);
            task.setTaskStatus(taskStatus);
        }

        Set<Label> labelSet = null;
        if (data.getTaskLabelIds() != null) {
            labelSet = labelRepository.findByIdIn((data.getTaskLabelIds()).get()).orElse(null);
        }
        task.setLabels(labelSet);

        taskRepository.save(task);
        return taskMapper.map(task);
    }

    public void deleteTask(Long taskId) {
        taskRepository.deleteById(taskId);
    }

}
