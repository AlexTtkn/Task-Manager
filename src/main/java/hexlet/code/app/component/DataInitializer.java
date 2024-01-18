package hexlet.code.app.component;

import hexlet.code.app.dto.LabelDTO.LabelCreateDTO;
import hexlet.code.app.dto.TaskStatusDTO.TaskStatusCreateDTO;
import hexlet.code.app.dto.UserDTO.UserCreateDTO;
import hexlet.code.app.mapper.LabelMapper;
import hexlet.code.app.mapper.TaskStatusMapper;
import hexlet.code.app.mapper.UserMapper;
import hexlet.code.app.repository.LabelRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private UserRepository userRepository;
    private UserMapper userMapper;
    private TaskStatusRepository taskStatusRepository;
    private TaskStatusMapper taskStatusMapper;
    private LabelRepository labelRepository;
    private LabelMapper labelMapper;
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        addAdminUser();
        addDefaultSlugs();
        addDefaultLabels();
    }

    private void addAdminUser() {
        var userData = new UserCreateDTO();
        userData.setEmail("hexlet@example.com");
        userData.setPasswordDigest("qwerty");
        var user = userMapper.map(userData);
        var hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPasswordDigest(hashedPassword);
        userRepository.save(user);
    }

    private void addDefaultSlugs() {
        List<String> defaultSlugs = List.of("draft", "to_review", "to_be_fixed", "to_publish", "published");
        defaultSlugs.forEach(slug -> {
            var statusData = new TaskStatusCreateDTO();
            String[] arr = slug.split("_");
            String first = arr[0].substring(0, 1).toUpperCase() + arr[0].substring(1);
            var name = new StringBuilder(first);

            if (arr.length > 1) {
                for (var element: arr) {
                    name.append(" ").append(element);
                }
            }

            statusData.setName(name.toString());
            statusData.setSlug(slug);
            var status = taskStatusMapper.map(statusData);
            taskStatusRepository.save(status);
        });
    }

    private void addDefaultLabels() {
        List<String> defaultLabels = List.of("feature", "bug");
        defaultLabels.forEach(name -> {
            var labelData = new LabelCreateDTO();
            labelData.setName(name);
            var label = labelMapper.map(labelData);
            labelRepository.save(label);
        });
    }
}
