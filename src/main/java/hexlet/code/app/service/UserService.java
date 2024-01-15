package hexlet.code.app.service;

import hexlet.code.app.dto.UserDTO.UserCreateDTO;
import hexlet.code.app.dto.UserDTO.UserDTO;
import hexlet.code.app.dto.UserDTO.UserUpdateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.UserMapper;
import hexlet.code.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;


    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::map)
                .toList();
    }

    public UserDTO createUser(UserCreateDTO dto) {
        var user = userMapper.map(dto);
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
        userRepository.save(user);
        return userMapper.map(user);
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

}
