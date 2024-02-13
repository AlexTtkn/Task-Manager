package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.UserDTO.UserCreateDTO;
import hexlet.code.dto.UserDTO.UserUpdateDTO;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.util.ModelGenerator;
import net.datafaker.Faker;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Faker faker;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ModelGenerator modelGenerator;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;

    private User testUser;

    @BeforeEach
    public void setUp() {
        token = jwt().jwt(builder -> builder.subject("hexlet@example.com"));
        testUser = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(testUser);
    }

    @AfterEach
    public void cleanUp() {
        taskRepository.deleteAll();
        if (testUser != null) {
            userRepository.deleteById(testUser.getId());
        }
    }

    @Test
    public void testIndex() throws Exception {
        var request = get("/api/users").with(token);
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();

        assertThatJson(body).isArray();
    }

    @Test
    public void testShow() throws Exception {
        var request = get("/api/users/{id}", testUser.getId()).with(jwt());

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("firstName").isEqualTo(testUser.getFirstName()),
                v -> v.node("lastName").isEqualTo(testUser.getLastName()),
                v -> v.node("email").isEqualTo(testUser.getEmail()),
                v -> v.node("createdAt").isEqualTo(testUser.getCreatedAt().format(ModelGenerator.FORMATTER))
        );
    }

    @Test
    public void testUpdate() throws Exception {
        var data = new UserUpdateDTO();
        data.setFirstName(JsonNullable.of(faker.name().firstName()));
        data.setLastName(JsonNullable.of(faker.name().lastName()));
        data.setEmail(JsonNullable.of(faker.internet().emailAddress()));
        data.setPassword(JsonNullable.of(faker.internet().password(3, 12)));

        token = jwt().jwt(builder -> builder.subject(testUser.getEmail()));

        var request = put("/api/users/{id}", testUser.getId()).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var updatedUser = userRepository.findById(testUser.getId()).orElse(null);

        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getEmail()).isEqualTo(data.getEmail().get());
        assertThat(updatedUser.getFirstName()).isEqualTo(data.getFirstName().get());
        assertThat(updatedUser.getLastName()).isEqualTo(data.getLastName().get());
        assertThat(updatedUser.getPassword()).isNotEqualTo(data.getPassword().get());
    }

    @Test
    public void testPartialUpdate() throws Exception {
        var data = new UserUpdateDTO();
        data.setFirstName(JsonNullable.of(faker.name().firstName()));
        data.setLastName(JsonNullable.of(faker.name().lastName()));

        token = jwt().jwt(builder -> builder.subject(testUser.getEmail()));

        var request = put("/api/users/{id}", testUser.getId()).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var updatedUser = userRepository.findById(testUser.getId()).orElse(null);

        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(updatedUser.getFirstName()).isEqualTo(data.getFirstName().get());
        assertThat(updatedUser.getLastName()).isEqualTo(data.getLastName().get());
        assertThat(updatedUser.getPassword()).isNotEqualTo(testUser.getPassword());
    }

    @Test
    public void testCreate() throws Exception {
        var data = new UserCreateDTO();
        data.setFirstName(faker.name().firstName());
        data.setLastName(faker.name().lastName());
        data.setEmail(faker.internet().emailAddress());
        data.setPassword(faker.internet().password(3, 12));

        var request = post("/api/users").with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        var result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        var body = result.getResponse().getContentAsString();

        var id = om.readTree(body).get("id").asLong();
        assertThat(userRepository.findById(id)).isPresent();

        var addedUser = userRepository.findById(id).orElse(null);

        assertThat(addedUser).isNotNull();
        assertThatJson(body).and(
                v -> v.node("id").isEqualTo(addedUser.getId()),
                v -> v.node("firstName").isEqualTo(addedUser.getFirstName()),
                v -> v.node("lastName").isEqualTo(addedUser.getLastName()),
                v -> v.node("email").isEqualTo(addedUser.getEmail()),
                v -> v.node("createdAt").isEqualTo(addedUser.getCreatedAt().format(ModelGenerator.FORMATTER))
        );
    }

    @Test
    public void testDestroy() throws Exception {
        token = jwt().jwt(builder -> builder.subject(testUser.getEmail()));

        var request = delete("/api/users/{id}", testUser.getId()).with(token);
        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        testUser = userRepository.findById(testUser.getId()).orElse(null);

        assertThat(testUser).isNull();
    }

    @Test
    public void testDestroyWithoutAuth() throws Exception {
        var request = delete("/api/users/{id}", testUser.getId());

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testDestroyButHasTask() throws Exception {
        var task = Instancio.of(modelGenerator.getTaskModel()).create();
        task.setAssignee(testUser);
        taskRepository.save(task);

        var user = task.getAssignee();

        token = jwt().jwt(builder -> builder.subject(user.getEmail()));

        mockMvc.perform(delete("/api/users/{id}", user.getId()).with(token))
                .andExpect(status().isMethodNotAllowed());
    }

}
