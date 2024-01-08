package hexlet.code.app.service;

import hexlet.code.app.dto.TaskStatusDTO.TaskStatusCreateDTO;
import hexlet.code.app.dto.TaskStatusDTO.TaskStatusDTO;
import hexlet.code.app.dto.TaskStatusDTO.TaskStatusUpdateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.TaskStatusMapper;
import hexlet.code.app.repository.TaskStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskStatusService {

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskStatusMapper taskStatusMapper;

    public List<TaskStatusDTO> getAllStatuses() {
        return taskStatusRepository.findAll().stream()
                .map(taskStatusMapper::map)
                .toList();
    }

    public TaskStatusDTO createStatus(TaskStatusCreateDTO dto) {
        var taskStatus = taskStatusMapper.map(dto);
        taskStatusRepository.save(taskStatus);
        return taskStatusMapper.map(taskStatus);
    }

    public TaskStatusDTO findById(Long statusId) {
        var taskStatus = taskStatusRepository.findById(statusId)
                .orElseThrow(() -> new ResourceNotFoundException("Status with id: " + statusId + " not found."));
        return taskStatusMapper.map(taskStatus);
    }

    public TaskStatusDTO updateStatus(Long statusId, TaskStatusUpdateDTO data) {
        var taskStatus = taskStatusRepository.findById(statusId)
                .orElseThrow(() -> new ResourceNotFoundException("Status with id: " + statusId + " not found."));
        taskStatusMapper.update(data, taskStatus);
        taskStatusRepository.save(taskStatus);
        return taskStatusMapper.map(taskStatus);
    }

    public void deleteStatus(Long statusId) {
        taskStatusRepository.deleteById(statusId);
    }
}
