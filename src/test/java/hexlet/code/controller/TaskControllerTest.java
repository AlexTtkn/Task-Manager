package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.TaskDTO.TaskCreateDTO;
import hexlet.code.dto.TaskDTO.TaskUpdateDTO;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.util.ModelGenerator;
import net.datafaker.Faker;
import net.javacrumbs.jsonunit.core.Option;
import org.instancio.Instancio;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    private LabelRepository labelRepository;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private ModelGenerator modelGenerator;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;

    private Task testTask;

    @BeforeEach
    public void setUp() {
        token = jwt().jwt(builder -> builder.subject("hexlet@example.com"));

    }

    private Task generateTask() {
        testTask = Instancio.of(modelGenerator.getTaskModel()).create();

        var testUser = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(testUser);
        testTask.setAssignee(testUser);

        var testTaskStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        taskStatusRepository.save(testTaskStatus);
        testTask.setTaskStatus(testTaskStatus);

        var testLabel = Instancio.of(modelGenerator.getLabelModel()).create();
        labelRepository.save(testLabel);
        testTask.getLabels().add(testLabel);

        return testTask;
    }

    @AfterEach
    public void cleanUp() {
        taskRepository.deleteAll();

        userRepository.deleteAll();
        labelRepository.deleteAll();
        taskStatusRepository.deleteAll();
    }

    @Test
    public void testIndex() throws Exception {
        testTask = generateTask();
        taskRepository.save(testTask);

        var request = get("/api/tasks").with(token);
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();

        assertThatJson(body).isArray();
    }

    @Test
    public void testIndexWithFilter() throws Exception {
        var task = generateTask();
        var titleCont = task.getName().substring(1).toLowerCase();
        var assigneeId = task.getAssignee().getId();
        var status = task.getTaskStatus().getSlug();
        var labelId = task.getLabels().iterator().next().getId();

        taskRepository.save(task);

        var request = get("/api/tasks"
                + "?"
                + "titleCont=" + titleCont
                + "&assigneeId=" + assigneeId
                + "&status=" + status
                + "&labelId=" + labelId)
                .with(token);

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var data = new HashMap<>();
        data.put("assignee_id", task.getAssignee().getId());
        data.put("content", task.getDescription());
        data.put("createdAt", task.getCreatedAt().format(ModelGenerator.FORMATTER));
        data.put("id", task.getId());
        data.put("index", task.getIndex());
        data.put("status", task.getTaskStatus().getSlug());
        data.put("title", task.getName());
        data.put("taskLabelIds", List.of(labelId));

        var body = result.getResponse().getContentAsString();

        assertThatJson(body).when(Option.IGNORING_ARRAY_ORDER)
                .isArray()
                .contains(om.writeValueAsString(data));
    }

    @Test
    public void testShow() throws Exception {
        testTask = generateTask();
        taskRepository.save(testTask);

        var request = get("/api/tasks/{id}", testTask.getId()).with(token);

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("id").isEqualTo(testTask.getId()),
                v -> v.node("index").isEqualTo(testTask.getIndex()),
                v -> v.node("title").isEqualTo(testTask.getName()),
                v -> v.node("content").isEqualTo(testTask.getDescription()),
                v -> v.node("status").isEqualTo(testTask.getTaskStatus().getSlug().toLowerCase()),
                v -> v.node("assignee_id").isEqualTo(testTask.getAssignee().getId()),
                v -> v.node("createdAt").isEqualTo(testTask.getCreatedAt().format(ModelGenerator.FORMATTER))

        );
    }

    @Test
    @Transactional
    public void testCreate() throws Exception {
        testTask = generateTask();

        var data = new TaskCreateDTO();
        data.setIndex(faker.number().randomNumber());
        data.setAssigneeId(testTask.getAssignee().getId());
        data.setTitle(faker.lorem().word() + "aa");
        data.setContent(faker.lorem().sentence());
        data.setStatus(testTask.getTaskStatus().getSlug());
        data.setTaskLabelIds(List.of(2L));

        var request = post("/api/tasks").with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        var result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        var body = result.getResponse().getContentAsString();

        var id = om.readTree(body).get("id").asLong();
        assertThat(taskRepository.findById(id)).isPresent();

        var addedTask = taskRepository.findById(id).orElse(null);
        assertThat(addedTask).isNotNull();
        assertThatJson(body).and(
                v -> v.node("id").isEqualTo(addedTask.getId()),
                v -> v.node("index").isEqualTo(addedTask.getIndex()),
                v -> v.node("assignee_id").isEqualTo(addedTask.getAssignee().getId()),
                v -> v.node("status").isEqualTo(addedTask.getTaskStatus().getSlug()),
                v -> v.node("content").isEqualTo(addedTask.getDescription()),
                v -> v.node("title").isEqualTo(addedTask.getName()),
                v -> v.node("createdAt").isEqualTo(addedTask.getCreatedAt().format(ModelGenerator.FORMATTER)),
                v -> v.node("taskLabelIds").isEqualTo(addedTask.getLabels().stream()
                        .map(Label::getId)
                        .toArray())
        );
    }


    @Test
    @Transactional
    public void testUpdate() throws Exception {
        testTask = generateTask();
        taskRepository.save(testTask);

        var updateDTO = new TaskUpdateDTO();
        updateDTO.setIndex(JsonNullable.of(faker.number().randomNumber()));
        updateDTO.setAssigneeId(JsonNullable.of(testTask.getAssignee().getId()));
        updateDTO.setTitle(JsonNullable.of(faker.lorem().word() + "aa"));
        updateDTO.setContent(JsonNullable.of(faker.lorem().sentence()));
        updateDTO.setStatus(JsonNullable.of(faker.lorem().word()));

        var request = put("/api/tasks/{id}", testTask.getId()).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateDTO));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var updatedTask = taskRepository.findById(testTask.getId()).orElse(null);

        assertThat(updatedTask).isNotNull();
        assertThat(updatedTask.getIndex()).isEqualTo(updateDTO.getIndex().get());
        assertThat(updatedTask.getName()).isEqualTo(updateDTO.getTitle().get());
        assertThat(updatedTask.getDescription()).isEqualTo(updateDTO.getContent().get());
        assertThat(updatedTask.getTaskStatus().getSlug()).isEqualTo(updateDTO.getStatus().get());
        assertThat(updatedTask.getAssignee().getId()).isEqualTo(updateDTO.getAssigneeId().get());
    }

    @Test
    public void testDestroy() throws Exception {
        testTask = generateTask();
        taskRepository.save(testTask);

        var request = delete("/api/tasks/{id}", testTask.getId()).with(token);

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        testTask = taskRepository.findById(testTask.getId()).orElse(null);

        assertThat(testTask).isNull();
    }

    @Test
    public void testDestroyWithoutAuth() throws Exception {
        testTask = generateTask();
        taskRepository.save(testTask);

        var request = delete("/api/tasks/{id}", testTask.getId());

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }
}
