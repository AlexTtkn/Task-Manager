package hexlet.code.app.controller;

import hexlet.code.app.dto.UserDTO.UserCreateDto;
import hexlet.code.app.dto.UserDTO.UserDto;
import hexlet.code.app.dto.UserDTO.UserUpdateDto;
import hexlet.code.app.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public List<UserDto> index() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserDto show(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody UserCreateDto dto) {
        return userService.createUser(dto);
    }

    @PutMapping("/{id}")
    public UserDto update(@PathVariable Long id, @Valid @RequestBody UserUpdateDto dto) {
        return userService.updateUser(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
