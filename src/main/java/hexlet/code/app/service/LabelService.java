package hexlet.code.app.service;

import hexlet.code.app.dto.LabelDTO.LabelCreateDTO;
import hexlet.code.app.dto.LabelDTO.LabelDTO;
import hexlet.code.app.dto.LabelDTO.LabelUpdateDTO;
import hexlet.code.app.exception.MethodNotAllowedException;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.LabelMapper;
import hexlet.code.app.repository.LabelRepository;
import hexlet.code.app.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LabelService {

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private LabelMapper labelMapper;

    @Autowired
    private TaskRepository taskRepository;

    public List<LabelDTO> getAllLabels() {
        return labelRepository.findAll().stream()
                .map(labelMapper::map)
                .toList();
    }

    public LabelDTO createLabel(LabelCreateDTO dto) {
        var label = labelMapper.map(dto);
        labelRepository.save(label);
        return labelMapper.map(label);
    }

    public LabelDTO findById(Long labelId) {
        var label = labelRepository.findById(labelId)
                .orElseThrow(() -> new ResourceNotFoundException("Label with id: " + labelId + " not found."));
        return labelMapper.map(label);
    }

    public LabelDTO updateLabel(Long labelId, LabelUpdateDTO data) {
        var label = labelRepository.findById(labelId)
                .orElseThrow(() -> new ResourceNotFoundException("Label with id: " + labelId + " not found."));
        labelMapper.update(data, label);
        labelRepository.save(label);
        return labelMapper.map(label);
    }

    public void deleteLabel(Long labelId) {
        var label = labelRepository.findById(labelId);

        if (label.isPresent() && taskRepository.findByLabelsName(label.get().getName()).isPresent()) {
            throw new MethodNotAllowedException("Label still has task");
        }
        labelRepository.deleteById(labelId);
    }

}
