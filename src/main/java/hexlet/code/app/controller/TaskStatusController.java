package hexlet.code.app.controller;

import hexlet.code.app.dto.TaskStatusDTO.TaskStatusCreateDTO;
import hexlet.code.app.dto.TaskStatusDTO.TaskStatusDTO;
import hexlet.code.app.dto.TaskStatusDTO.TaskStatusUpdateDTO;
import hexlet.code.app.service.TaskStatusService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/task_statuses")
public class TaskStatusController {

    @Autowired
    private TaskStatusService taskStatusService;

    @GetMapping
    public List<TaskStatusDTO> index() {
        return taskStatusService.getAllStatuses();
    }

    @GetMapping("/{id}")
    public TaskStatusDTO show(@PathVariable Long id) {
        return taskStatusService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskStatusDTO create(@Valid @RequestBody TaskStatusCreateDTO dto) {
        return taskStatusService.createStatus(dto);
    }

    @PutMapping("/{id}")
    public TaskStatusDTO update(@PathVariable Long id, @Valid @RequestBody TaskStatusUpdateDTO dto) {
        return taskStatusService.updateStatus(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        taskStatusService.deleteStatus(id);
    }

}
