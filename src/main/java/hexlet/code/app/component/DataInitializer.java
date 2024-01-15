package hexlet.code.app.component;

import hexlet.code.app.dto.TaskStatusDTO.TaskStatusCreateDTO;
import hexlet.code.app.dto.UserDTO.UserCreateDTO;
import hexlet.code.app.mapper.TaskStatusMapper;
import hexlet.code.app.mapper.UserMapper;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private final TaskStatusRepository taskStatusRepository;

    @Autowired
    private final TaskStatusMapper taskStatusMapper;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        var userData = new UserCreateDTO();
        userData.setEmail("hexlet@example.com");
        userData.setPasswordDigest("qwerty");
        var user = userMapper.map(userData);
        var hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPasswordDigest(hashedPassword);
        userRepository.save(user);

        List<String> defaultSlugs = List.of(
                "draft", "to_review", "to_be_fixed", "to_publish", "published"
        );

        defaultSlugs.forEach(slug -> {
            var taskStatusData = new TaskStatusCreateDTO();
            String[] arr = slug.split("_");
            String first = arr[0].substring(0, 1).toUpperCase() + arr[0].substring(1);
            var name = new StringBuilder(first);

            if (arr.length > 1) {
                for (int i = 1; i < arr.length; i++) {
                    name.append(" ").append(arr[i]);
                }
            }

            taskStatusData.setName(name.toString());
            taskStatusData.setSlug(slug);
            var taskStatus = taskStatusMapper.map(taskStatusData);
            taskStatusRepository.save(taskStatus);
        });
    }
}
