package hexlet.code.service;

import hexlet.code.dto.UserDTO.UserCreateDTO;
import hexlet.code.dto.UserDTO.UserDTO;
import hexlet.code.dto.UserDTO.UserUpdateDTO;
import hexlet.code.exception.MethodNotAllowedException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.UserMapper;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {

    private UserRepository userRepository;
    private UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private TaskRepository taskRepository;

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::map)
                .toList();
    }

    public UserDTO createUser(UserCreateDTO dto) {
        var user = userMapper.map(dto);
        var hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);
        userRepository.save(user);
        return userMapper.map(user);
    }

    public UserDTO findById(Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id: " + userId + " not found."));

        return userMapper.map(user);
    }

    public UserDTO updateUser(Long userId, UserUpdateDTO data) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id: " + userId + " not found."));
        userMapper.update(data, user);

        var hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);

        userRepository.save(user);
        return userMapper.map(user);
    }

    public void deleteUser(Long userId) {
        var user = userRepository.findById(userId);

        if (user.isPresent() && taskRepository.findByAssigneeEmail(user.get().getEmail()).isPresent()) {
            throw new MethodNotAllowedException("User still has task");
        }
        userRepository.deleteById(userId);
    }

}
