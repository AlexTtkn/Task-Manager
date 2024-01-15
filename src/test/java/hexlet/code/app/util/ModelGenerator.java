package hexlet.code.app.util;


import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.model.User;
import jakarta.annotation.PostConstruct;


import lombok.Getter;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Model;
import org.instancio.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ModelGenerator {
    private Model<User> userModel;
    private Model<TaskStatus> taskStatusModel;

    @Autowired
    private Faker faker;

    @PostConstruct
    private void init() {
        taskStatusModel = Instancio.of(TaskStatus.class)
                .ignore(Select.field(TaskStatus::getId))
                .supply(Select.field(TaskStatus::getName), () -> "To test")
                .supply(Select.field(TaskStatus::getSlug), () -> "to_test")
                .toModel();

        userModel = Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress())
                .supply(Select.field(User::getFirstname), () -> faker.name().firstName())
                .supply(Select.field(User::getLastname), () -> faker.name().lastName())
                .supply(Select.field(User::getPasswordDigest), () -> faker.internet().password(3, 12))
                .toModel();
    }
}
