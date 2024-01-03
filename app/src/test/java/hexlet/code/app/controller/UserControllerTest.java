package hexlet.code.app.controller;

import hexlet.code.app.mapper.UserMapper;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.UserRepository;
import org.junit.jupiter.api.Test;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

import org.instancio.Instancio;
import org.instancio.Select;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.datafaker.Faker;


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
    private UserMapper userMapper;


    private User generateUser() {
        return Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .supply(Select.field(User::getFirstname), () -> faker.name().firstName())
                .supply(Select.field(User::getLastname), () -> faker.name().lastName())
                .supply(Select.field(User::getPassword), () -> faker.internet().password(3, 12))
                .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress())
                .create();
    }

    @Test
    public void testShow() throws Exception {
        var testUser = generateUser();
        userRepository.save(testUser);

        var request = get("/api/users/{id}", testUser.getId());
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("firstname").isEqualTo(testUser.getFirstname()),
                v -> v.node("lastname").isEqualTo(testUser.getLastname()),
                v -> v.node("email").isEqualTo(testUser.getEmail()),
                v -> v.node("createdAt").isEqualTo(testUser.getCreatedAt()),
                v -> v.node("updatedAt").isEqualTo(testUser.getUpdatedAt())
        );
    }

    @Test
    public void testShowUserNotFound() throws Exception {
        Long id = 100L;
        userRepository.deleteById(id);

        var request = get("/api/users/{id}", id);
        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    public void testIndex() throws Exception {
        var testUser = generateUser();
        userRepository.save(testUser);

        var result = mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    public void testCreate() throws Exception {
        var data = Map.of(
                "email", faker.internet().emailAddress(),
                "firstname", faker.name().firstName(),
                "lastname", faker.name().lastName(),
                "password", faker.internet().password(3, 12)
        );

        var request = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));
        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var user = userRepository.findByEmail(data.get("email")).orElse(null);

        assertThat(user).isNotNull();
        assertThat(user.getFirstname()).isEqualTo(data.get("firstname"));
        assertThat(user.getLastname()).isEqualTo(data.get("lastname"));
        assertThat(user.getEmail()).isEqualTo(data.get("email"));
        assertThat(user.getPassword()).isEqualTo(data.get("password"));
    }

    @Test
    public void testCreateWithoutFirstNameAndLastName() throws Exception {
        var dataWithoutFirstNameAndLastName = Map.of(
                "email", faker.internet().emailAddress(),
                "password", faker.internet().password(3, 12)
        );

        var request = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dataWithoutFirstNameAndLastName));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var user = userRepository.findByEmail(dataWithoutFirstNameAndLastName.get("email")).orElse(null);

        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(dataWithoutFirstNameAndLastName.get("email"));
        assertThat(user.getPassword()).isEqualTo(dataWithoutFirstNameAndLastName.get("password"));
    }

    @Test
    public void testCreateWithInvalidPassword() throws Exception {
        var dataWithInvalidPassword = Map.of(
                "email", faker.internet().emailAddress(),
                "firstname", faker.name().firstName(),
                "lastname", faker.name().lastName(),
                "password", faker.internet().password(1, 2)
        );

        var request = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dataWithInvalidPassword));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateWithInvalidEmail() throws Exception {
        var dataWithInvalidEmail = Map.of(
                "email", faker.name().username(),
                "firstname", faker.name().firstName(),
                "lastname", faker.name().lastName(),
                "password", faker.internet().password(3, 12)
        );

        var request = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dataWithInvalidEmail));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdate() throws Exception {
        var testUser = generateUser();
        userRepository.save(testUser);

        var data = Map.of(
                "email", faker.internet().emailAddress(),
                "firstname", faker.name().firstName(),
                "lastname", faker.name().lastName(),
                "password", faker.internet().password(3, 12)
        );

        var request = put("/api/users/" + testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var updatedUser = userRepository.findById(testUser.getId()).orElse(null);

        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getEmail()).isEqualTo(data.get("email"));
        assertThat(updatedUser.getFirstname()).isEqualTo(data.get("firstname"));
        assertThat(updatedUser.getLastname()).isEqualTo(data.get("lastname"));
        assertThat(updatedUser.getPassword()).isEqualTo(data.get("password"));
    }

    @Test
    public void testPartialUpdate() throws Exception {
        var testUser = generateUser();
        userRepository.save(testUser);

        var data = Map.of(
                "firstname", faker.name().firstName(),
                "lastname", faker.name().lastName()
        );

        var request = put("/api/users/" + testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var updatedUser = userRepository.findById(testUser.getId()).orElse(null);

        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(updatedUser.getFirstname()).isEqualTo(data.get("firstname"));
        assertThat(updatedUser.getLastname()).isEqualTo(data.get("lastname"));
        assertThat(updatedUser.getPassword()).isEqualTo(testUser.getPassword());

    }

    @Test
    public void testDeleteUser() throws Exception {
        var testUser = generateUser();
        userRepository.save(testUser);

        var request = delete("/api/users/{id}", testUser.getId());
        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        testUser = userRepository.findById(testUser.getId()).orElse(null);
        assertThat(testUser).isNull();
    }

}
