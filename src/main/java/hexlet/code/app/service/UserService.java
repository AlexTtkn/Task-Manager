package hexlet.code.app.service;

import hexlet.code.app.dto.UserDTO.UserCreateDto;
import hexlet.code.app.dto.UserDTO.UserDto;
import hexlet.code.app.dto.UserDTO.UserUpdateDto;
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


    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::map)
                .toList();
    }

    public UserDto createUser(UserCreateDto dto) {
        var user = userMapper.map(dto);
        userRepository.save(user);
        return userMapper.map(user);
    }

    public UserDto findById(Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id: " + userId + " not found."));

        return userMapper.map(user);
    }

    public UserDto updateUser(Long userId, UserUpdateDto data) {
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
