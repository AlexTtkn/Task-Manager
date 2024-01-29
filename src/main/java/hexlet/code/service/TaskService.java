package hexlet.code.service;

import hexlet.code.dto.TaskDTO.TaskCreateDTO;
import hexlet.code.dto.TaskDTO.TaskDTO;
import hexlet.code.dto.TaskDTO.TaskParamsDTO;
import hexlet.code.dto.TaskDTO.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.specification.TaskSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LabelRepository labelRepository;


    @Autowired
    private TaskSpecification specBuilder;

    public List<TaskDTO> getAllTasks(TaskParamsDTO paramsDTO) {
        var specification = specBuilder.build(paramsDTO);
        return taskRepository.findAll(specification).stream()
                .map(taskMapper::map)
                .toList();
    }

    public TaskDTO createTask(TaskCreateDTO dto) {
        var task = taskMapper.map(dto);

        var assigneeId = dto.getAssigneeId();
        if (assigneeId != null) {
            var assignee = userRepository.findById(assigneeId).orElse(null);
            task.setAssignee(assignee);
        }

        var statusSlug = dto.getStatus();
        var taskStatus = taskStatusRepository.findBySlug(statusSlug).orElse(null);
        task.setTaskStatus(taskStatus);

        var labels = dto.getTaskLabelIds();
        if (!labels.isEmpty()) {
            var labelsSet = labelRepository.findByIdIn(labels).orElse(null);
            task.setLabels(labelsSet);
        }

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

        var assigneeId = data.getAssigneeId();
        if (assigneeId != null) {
            var assignee = userRepository.findById((assigneeId).get()).orElse(null);
            task.setAssignee(assignee);
            assert assignee != null;
            userRepository.save(assignee);
        }

        var statusSlug = data.getStatus();
        if (statusSlug != null) {
            var taskStatus = taskStatusRepository.findBySlug((statusSlug).get()).orElse(null);
            task.setTaskStatus(taskStatus);
            assert taskStatus != null;
            taskStatusRepository.save(taskStatus);

        }

        var labels = data.getTaskLabelIds().orElse(null);
        if (!labels.isEmpty()) {
            var labelsSet = labelRepository.findByIdIn(labels).orElse(null);
            task.setLabels(labelsSet);
        }

        taskRepository.save(task);
        return taskMapper.map(task);
    }

    public void deleteTask(Long taskId) {
        taskRepository.deleteById(taskId);
    }

}
