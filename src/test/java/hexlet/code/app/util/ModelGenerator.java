package hexlet.code.app.util;

import hexlet.code.app.model.Label;
import hexlet.code.app.model.Task;
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

import java.time.format.DateTimeFormatter;
import java.util.HashSet;

@Getter
@Component
public class ModelGenerator {
    private Model<User> userModel;
    private Model<TaskStatus> taskStatusModel;
    private Model<Task> taskModel;
    private Model<Label> labelModel;

    @Autowired
    private Faker faker;

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    @PostConstruct
    private void init() {
        labelModel = Instancio.of(Label.class)
                .ignore(Select.field(Label::getId))
                .ignore(Select.field(Label::getCreatedAt))
                .supply(Select.field(Label::getName), () -> faker.lorem().word() + "aa")
                .toModel();

        taskModel = Instancio.of(Task.class)
                .ignore(Select.field(Task::getId))
                .ignore(Select.field(Task::getCreatedAt))
                .ignore(Select.field(Task::getAssignee))
                .ignore(Select.field(Task::getTaskStatus))
                .supply(Select.field(Task::getName), () -> faker.lorem().word())
                .supply(Select.field(Task::getIndex), () -> faker.number().randomNumber())
                .supply(Select.field(Task::getDescription), () -> faker.lorem().sentence())
                .supply(Select.field(Task::getLabels), () -> new HashSet<Label>())
                .toModel();

        var slugAndName = faker.lorem().word();
        taskStatusModel = Instancio.of(TaskStatus.class)
                .ignore(Select.field(TaskStatus::getId))
                .ignore(Select.field(TaskStatus::getCreatedAt))
                .supply(Select.field(TaskStatus::getName), () -> slugAndName)
                .supply(Select.field(TaskStatus::getSlug), () -> slugAndName)
                .toModel();

        userModel = Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .ignore(Select.field(User::getCreatedAt))
                .ignore(Select.field(User::getUpdatedAt))
                .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress())
                .supply(Select.field(User::getFirstname), () -> faker.name().firstName())
                .supply(Select.field(User::getLastname), () -> faker.name().lastName())
                .supply(Select.field(User::getPasswordDigest), () -> faker.internet().password(3, 12))
                .toModel();
    }
}
