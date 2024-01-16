package hexlet.code.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.mapper.TaskStatusMapper;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.util.ModelGenerator;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

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
    private ObjectMapper om;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskStatusMapper taskStatusMapper;

    @Autowired
    private ModelGenerator modelGenerator;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;

    private TaskStatus testTaskStatus;

    @BeforeEach
    public void setUp() {
        token = jwt().jwt(builder -> builder.subject("hexlet@example.com"));
        testTaskStatus = Instancio.of(modelGenerator.getTaskStatusModel())
                .create();
        taskStatusRepository.save(testTaskStatus);
    }

    @AfterEach
    public void cleanUp() {
        taskStatusRepository.deleteById(testTaskStatus.getId());
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

        //в testTaskStatus есть поле createdAT, а в ответе это поле не приходит.

        assertThatJson(body).and(
                v -> v.node("name").isEqualTo(testTaskStatus.getName()),
                v -> v.node("slug").isEqualTo(testTaskStatus.getSlug())
//                v -> v.node("createdAt").isEqualTo(testTaskStatus.getCratedAt())
        );
    }

    @Test
    public void testUpdate() throws Exception {
        var data = Map.of(
                "name", "To test update",
                "slug", "to_test_update"
        );

        var request = put("/api/task_statuses/{id}", testTaskStatus.getId()).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var updatedTaskStatus = taskStatusRepository.findById(testTaskStatus.getId()).orElse(null);

        assertThat(updatedTaskStatus).isNotNull();
        assertThat(updatedTaskStatus.getName()).isEqualTo(data.get("name"));
        assertThat(updatedTaskStatus.getSlug()).isEqualTo(data.get("slug"));
    }

    @Test
    public void testCreate() throws Exception {
        var data = Map.of(
                "name", "To test create",
                "slug", "to_test_create"
        );

        var request = post("/api/task_statuses").with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var taskStatus = taskStatusRepository.findBySlug(data.get("slug")).orElse(null);

        assertThat(taskStatus).isNotNull();
        assertThat(taskStatus.getName()).isEqualTo(data.get("name"));
        assertThat(taskStatus.getSlug()).isEqualTo(data.get("slug"));
    }

    @Test
    public void testDestroy() throws Exception {
        System.out.println(testTaskStatus.getId());
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

}
