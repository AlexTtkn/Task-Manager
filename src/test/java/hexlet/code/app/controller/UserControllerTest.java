package hexlet.code.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.mapper.UserMapper;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.UserRepository;
import hexlet.code.app.util.ModelGenerator;
import net.datafaker.Faker;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    private UserMapper userMapper;

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

//    @AfterEach
//    public void cleanUp() {
//        userRepository.deleteById(testUser.getId());
//    }


    @Test
    public void testShow() throws Exception {

        var request = get("/api/users/{id}", testUser.getId()).with(jwt());
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("firstname").isEqualTo(testUser.getFirstname()),
                v -> v.node("lastname").isEqualTo(testUser.getLastname()),
                v -> v.node("email").isEqualTo(testUser.getEmail()),
                v -> v.node("createdAt").isEqualTo(testUser.getCreatedAt())
        );
    }

    @Test
    public void testShowUserNotFound() throws Exception {
        Long id = 100L;
        userRepository.deleteById(id);

        var request = get("/api/users/{id}", id).with(token);
        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    public void testIndex() throws Exception {
        var result = mockMvc.perform(get("/api/users").with(token))
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
                "passwordDigest", faker.internet().password(3, 12)
        );

        var request = post("/api/users").with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var user = userRepository.findByEmail(data.get("email")).orElse(null);

        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(data.get("email"));
        assertThat(user.getFirstname()).isEqualTo(data.get("firstname"));
        assertThat(user.getLastname()).isEqualTo(data.get("lastname"));
        assertThat(user.getPasswordDigest()).isNotEqualTo(data.get("passwordDigest"));
    }


    @Test
    public void testCreateWithoutFirstNameAndLastName() throws Exception {
        var dataWithoutFirstNameAndLastName = Map.of(
                "email", faker.internet().emailAddress(),
                "passwordDigest", faker.internet().password(3, 12)
        );

        var request = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dataWithoutFirstNameAndLastName));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var user = userRepository.findByEmail(dataWithoutFirstNameAndLastName.get("email")).orElse(null);

        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(dataWithoutFirstNameAndLastName.get("email"));
        assertThat(user.getPasswordDigest()).isNotEqualTo(dataWithoutFirstNameAndLastName.get("passwordDigest"));
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
        var data = Map.of(
                "email", faker.internet().emailAddress(),
                "firstname", faker.name().firstName(),
                "lastname", faker.name().lastName(),
                "passwordDigest", faker.internet().password(3, 12)
        );

        var jwt = jwt().jwt(builder -> builder.subject(testUser.getEmail()));
        var request = put("/api/users/{id}", testUser.getId()).with(jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var updatedUser = userRepository.findById(testUser.getId()).orElse(null);

        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getEmail()).isEqualTo(data.get("email"));
        assertThat(updatedUser.getFirstname()).isEqualTo(data.get("firstname"));
        assertThat(updatedUser.getLastname()).isEqualTo(data.get("lastname"));
        assertThat(updatedUser.getPasswordDigest()).isNotEqualTo(data.get("passwordDigest"));
    }

    @Test
    public void testPartialUpdate() throws Exception {
        var data = Map.of(
                "firstname", faker.name().firstName(),
                "lastname", faker.name().lastName()
        );

        var jwt = jwt().jwt(builder -> builder.subject(testUser.getEmail()));
        var request = put("/api/users/{id}", testUser.getId()).with(jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var updatedUser = userRepository.findById(testUser.getId()).orElse(null);

        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(updatedUser.getFirstname()).isEqualTo(data.get("firstname"));
        assertThat(updatedUser.getLastname()).isEqualTo(data.get("lastname"));
        assertThat(updatedUser.getPasswordDigest()).isEqualTo(testUser.getPasswordDigest());

    }
    @Test
    public void testDestroy() throws Exception {
        var jwt = jwt().jwt(builder -> builder.subject(testUser.getEmail()));
        var request = delete("/api/users/{id}", testUser.getId()).with(jwt);
        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        var user = userRepository.findById(testUser.getId()).orElse(null);
        assertThat(user).isNull();
    }

    @Test
    public void testDestroyWithoutAuth() throws Exception {
        var request = delete("/api/users/{id}", testUser.getId());
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

}
