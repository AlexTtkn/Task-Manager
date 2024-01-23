package hexlet.code.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.dto.UserDTO.UserCreateDTO;
import hexlet.code.app.dto.UserDTO.UserUpdateDTO;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.UserRepository;
import hexlet.code.app.util.ModelGenerator;
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
        if (testUser != null) {
            userRepository.deleteById(testUser.getId());
        }
//        var userTasks = testUser.getTasks();
//
//        if (userTasks.isEmpty()) {
//            userRepository.deleteById(testUser.getId());
//        }

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
                v -> v.node("firstname").isEqualTo(testUser.getFirstname()),
                v -> v.node("lastname").isEqualTo(testUser.getLastname()),
                v -> v.node("email").isEqualTo(testUser.getEmail()),
                v -> v.node("createdAt").isEqualTo(testUser.getCreatedAt().format(ModelGenerator.FORMATTER))
        );
    }

//    @Test
//    public void testShowUserNotFound() throws Exception {
//        Long id = 100L;
//        userRepository.deleteById(id);
//
//        var request = get("/api/users/{id}", id).with(token);
//        mockMvc.perform(request)
//                .andExpect(status().isNotFound());
//    }


    @Test
    public void testUpdate() throws Exception {
        var data = new UserUpdateDTO();
        data.setFirstname(JsonNullable.of(faker.name().firstName()));
        data.setLastname(JsonNullable.of(faker.name().lastName()));
        data.setEmail(JsonNullable.of(faker.internet().emailAddress()));
        data.setPasswordDigest(JsonNullable.of(faker.internet().password(3, 12)));

        var request = put("/api/users/{id}", testUser.getId()).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var updatedUser = userRepository.findById(testUser.getId()).orElse(null);

        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getEmail()).isEqualTo(data.getEmail().get());
        assertThat(updatedUser.getFirstname()).isEqualTo(data.getFirstname().get());
        assertThat(updatedUser.getLastname()).isEqualTo(data.getLastname().get());
        assertThat(updatedUser.getPasswordDigest()).isNotEqualTo(data.getPasswordDigest().get());
    }

    @Test
    public void testCreate() throws Exception {
        var data = new UserCreateDTO();
        data.setFirstname(faker.name().firstName());
        data.setLastname(faker.name().lastName());
        data.setEmail(faker.internet().emailAddress());
        data.setPasswordDigest(faker.internet().password(3, 12));

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
                v -> v.node("firstname").isEqualTo(addedUser.getFirstname()),
                v -> v.node("lastname").isEqualTo(addedUser.getLastname()),
                v -> v.node("email").isEqualTo(addedUser.getEmail()),
                v -> v.node("createdAt").isEqualTo(addedUser.getCreatedAt().format(ModelGenerator.FORMATTER))
        );
    }

//    @Test
//    public void testCreateWithoutFirstNameAndLastName() throws Exception {
//        var dataWithoutFirstNameAndLastName = Map.of(
//                "email", faker.internet().emailAddress(),
//                "passwordDigest", faker.internet().password(3, 12)
//        );
//
//        var request = post("/api/users")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(om.writeValueAsString(dataWithoutFirstNameAndLastName));
//
//        mockMvc.perform(request)
//                .andExpect(status().isCreated());
//
//        var user = userRepository.findByEmail(dataWithoutFirstNameAndLastName.get("email")).orElse(null);
//
//        assertThat(user).isNotNull();
//        assertThat(user.getEmail()).isEqualTo(dataWithoutFirstNameAndLastName.get("email"));
//        assertThat(user.getPasswordDigest()).isNotEqualTo(dataWithoutFirstNameAndLastName.get("passwordDigest"));
//    }

//    @Test
//    public void testCreateWithInvalidPassword() throws Exception {
//        var dataWithInvalidPassword = Map.of(
//                "email", faker.internet().emailAddress(),
//                "firstname", faker.name().firstName(),
//                "lastname", faker.name().lastName(),
//                "password", faker.internet().password(1, 2)
//        );
//
//        var request = post("/api/users")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(om.writeValueAsString(dataWithInvalidPassword));
//
//        mockMvc.perform(request)
//                .andExpect(status().isBadRequest());
//    }

//    @Test
//    public void testCreateWithInvalidEmail() throws Exception {
//        var dataWithInvalidEmail = Map.of(
//                "email", faker.name().username(),
//                "firstname", faker.name().firstName(),
//                "lastname", faker.name().lastName(),
//                "password", faker.internet().password(3, 12)
//        );
//
//        var request = post("/api/users")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(om.writeValueAsString(dataWithInvalidEmail));
//
//        mockMvc.perform(request)
//                .andExpect(status().isBadRequest());
//    }

    @Test
    public void testDestroy() throws Exception {
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

}
