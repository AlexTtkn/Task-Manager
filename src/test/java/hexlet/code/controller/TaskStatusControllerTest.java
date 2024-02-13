package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.TaskStatusDTO.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusDTO.TaskStatusUpdateDTO;
import hexlet.code.model.Label;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.util.ModelGenerator;
import net.datafaker.Faker;
import org.assertj.core.api.Assertions;
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
class TaskStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Faker faker;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ModelGenerator modelGenerator;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;

    private TaskStatus testTaskStatus;

    @BeforeEach
    public void setUp() {
        token = jwt().jwt(builder -> builder.subject("hexlet@example.com"));
        testTaskStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        taskStatusRepository.save(testTaskStatus);
    }

    @AfterEach
    public void cleanUp() {
        taskRepository.deleteAll();
        if (testTaskStatus != null) {
            taskStatusRepository.deleteById(testTaskStatus.getId());
        }
    }

    @Test
    public void testIndex() throws Exception {
        var request = get("/api/task_statuses").with(token);
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();

        assertThatJson(body).isArray();
    }

    @Test
    public void testShow() throws Exception {
        var request = get("/api/task_statuses/{id}", testTaskStatus.getId()).with(jwt());

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("name").isEqualTo(testTaskStatus.getName()),
                v -> v.node("slug").isEqualTo(testTaskStatus.getSlug()),
                v -> v.node("createdAt").isEqualTo(testTaskStatus.getCreatedAt().format(ModelGenerator.FORMATTER))
        );
    }

    @Test
    public void testUpdate() throws Exception {
        var data = new TaskStatusUpdateDTO();
        data.setName(JsonNullable.of(faker.name().name()));
        data.setSlug(JsonNullable.of(faker.lorem().word()));

        var request = put("/api/task_statuses/{id}", testTaskStatus.getId()).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var updatedTaskStatus = taskStatusRepository.findById(testTaskStatus.getId()).orElse(null);

        Assertions.assertThat(updatedTaskStatus).isNotNull();
        Assertions.assertThat(updatedTaskStatus.getName()).isEqualTo(data.getName().get());
        Assertions.assertThat(updatedTaskStatus.getSlug()).isEqualTo(data.getSlug().get());
    }

    @Test
    public void testPartialUpdate() throws Exception {
        var data = new TaskStatusUpdateDTO();
        data.setName(JsonNullable.of(faker.name().name()));


        var request = put("/api/task_statuses/{id}", testTaskStatus.getId()).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var updatedTaskStatus = taskStatusRepository.findById(testTaskStatus.getId()).orElse(null);

        Assertions.assertThat(updatedTaskStatus).isNotNull();
        Assertions.assertThat(updatedTaskStatus.getName()).isEqualTo(data.getName().get());
        Assertions.assertThat(updatedTaskStatus.getSlug()).isEqualTo(testTaskStatus.getSlug());
    }

    @Test
    public void testCreate() throws Exception {
        var data = new TaskStatusCreateDTO();
        data.setName(faker.name().name());
        data.setSlug(faker.lorem().word());

        var request = post("/api/task_statuses").with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        var result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        var body = result.getResponse().getContentAsString();

        var id = om.readTree(body).get("id").asLong();
        Assertions.assertThat(taskStatusRepository.findById(id)).isPresent();

        var addedTaskStatus = taskStatusRepository.findById(id).orElse(null);

        Assertions.assertThat(addedTaskStatus).isNotNull();
        assertThatJson(body).and(
                v -> v.node("id").isEqualTo(addedTaskStatus.getId()),
                v -> v.node("name").isEqualTo(addedTaskStatus.getName()),
                v -> v.node("createdAt").isEqualTo(addedTaskStatus.getCreatedAt().format(ModelGenerator.FORMATTER))
        );
    }

    @Test
    public void testDestroy() throws Exception {
        var request = delete("/api/task_statuses/{id}", testTaskStatus.getId()).with(token);

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        testTaskStatus = taskStatusRepository.findById(testTaskStatus.getId()).orElse(null);

        assertThat(testTaskStatus).isNull();
    }

    @Test
    public void testDestroyWithoutAuth() throws Exception {
        var request = delete("/api/task_statuses/{id}", testTaskStatus.getId());

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testDestroyButHasTask() throws Exception {
        var task = Instancio.of(modelGenerator.getTaskModel()).create();
        task.setTaskStatus(testTaskStatus);
        taskRepository.save(task);

        var taskStatus = task.getTaskStatus();

        mockMvc.perform(delete("/api/task_statuses/{id}", taskStatus.getId()).with(token))
                .andExpect(status().isMethodNotAllowed());
    }

}
