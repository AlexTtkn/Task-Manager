package hexlet.code.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.dto.TaskDTO.TaskCreateDTO;
import hexlet.code.app.dto.TaskDTO.TaskUpdateDTO;
import hexlet.code.app.mapper.TaskMapper;
import hexlet.code.app.model.Task;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import hexlet.code.app.util.ModelGenerator;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Faker faker;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private ModelGenerator modelGenerator;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;

    @BeforeEach
    public void setUp() {
        token = jwt().jwt(builder -> builder.subject("hexlet@example.com"));
    }

    @AfterEach
    public void cleanUp() {
        taskRepository.deleteAll();
    }


    private User generateUser() {
        return Instancio.of(modelGenerator.getUserModel()).create();
    }
    private Task generateTask() {
        var user = userRepository.findById(1L).get();
        var taskStatus = taskStatusRepository.findBySlug("draft").get();
        return Instancio.of(Task.class)
                .ignore(Select.field(User::getId))
                .supply(Select.field(Task::getIndex), () -> (Long) faker.number().randomNumber())
                .supply(Select.field(Task::getAssignee), () -> user)
                .supply(Select.field(Task::getName), () -> faker.lorem().word())
                .supply(Select.field(Task::getDescription), () -> faker.lorem().sentence())
                .supply(Select.field(Task::getTaskStatus), () -> taskStatus)
                .supply(Select.field(Task::getAssignee), () -> user)
                .create();

    }

    @Test
    public void testShow() throws Exception {
        var testTask = generateTask();
        taskRepository.save(testTask);

        var request = get("/api/tasks/{id}", testTask.getId()).with(token);
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("index").isEqualTo(testTask.getIndex()),
                v -> v.node("title").isEqualTo(testTask.getName()),
                v -> v.node("content").isEqualTo(testTask.getDescription()),
                v -> v.node("status").isEqualTo(testTask.getTaskStatus().getName().toLowerCase()),
                v -> v.node("assignee_id").isEqualTo(testTask.getAssignee().getId())
        );
    }

    @Test
    public void testIndex() throws Exception {
        var testTask = generateTask();
        taskRepository.save(testTask);

        var request = get("/api/tasks").with(token);
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    public void testCreate() throws Exception {
        var createDTO = new TaskCreateDTO();
        createDTO.setIndex(faker.number().randomNumber());
        createDTO.setAssigneeId(1L);
        createDTO.setTitle(faker.lorem().word());
        createDTO.setContent(faker.lorem().sentence());
        createDTO.setStatus("draft");

        var request = post("/api/tasks").with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(createDTO));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var task = taskRepository.findByName(createDTO.getTitle()).orElse(null);

        assertThat(task).isNotNull();
        assertThat(task.getIndex()).isEqualTo(createDTO.getIndex());
        assertThat(task.getName()).isEqualTo(createDTO.getTitle());
        assertThat(task.getDescription()).isEqualTo(createDTO.getContent());
        assertThat(task.getTaskStatus().getName()).isEqualTo(createDTO.getStatus());
        assertThat(task.getAssignee().getId()).isEqualTo(createDTO.getAssigneeId());
    }


    @Test
    public void testUpdate() throws Exception {
        var testUser = generateUser();
        userRepository.save(testUser);

        var testTask = generateTask();
        taskStatusRepository.save(testTask.getTaskStatus());
        taskRepository.save(testTask);


        var updateDTO = new TaskUpdateDTO();
        updateDTO.setIndex(JsonNullable.of(faker.number().randomNumber()));
        updateDTO.setAssigneeId(JsonNullable.of(testUser.getId()));
        updateDTO.setTitle(JsonNullable.of(faker.lorem().word()));
        updateDTO.setContent(JsonNullable.of(faker.lorem().sentence()));
        updateDTO.setStatus(JsonNullable.of("published"));

        var request = put("/api/tasks/{id}", testTask.getId()).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateDTO));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var updatedTask = taskRepository.findById(testTask.getId()).orElse(null);

        assertThat(updatedTask).isNotNull();
        assertThat(updatedTask.getIndex()).isEqualTo(updateDTO.getIndex().get());
        assertThat(updatedTask.getAssignee().getId()).isEqualTo(updateDTO.getAssigneeId().get());
        assertThat(updatedTask.getName()).isEqualTo(updateDTO.getTitle().get());
        assertThat(updatedTask.getDescription()).isEqualTo(updateDTO.getContent().get());
        assertThat(updatedTask.getTaskStatus().getSlug()).isEqualTo(updateDTO.getStatus().get());
    }

    @Test
    public void testDestroy() throws Exception {
        var testTask = generateTask();
        taskRepository.save(testTask);

        var request = delete("/api/tasks/{id}", testTask.getId()).with(token);
        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        testTask = taskRepository.findById(testTask.getId()).orElse(null);
        assertThat(testTask).isNull();
    }

    @Test
    public void testDestroyWithoutAuth() throws Exception {
        var testTask = generateTask();
        taskRepository.save(testTask);

        var request = delete("/api/tasks/{id}", testTask.getId());
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }
}
