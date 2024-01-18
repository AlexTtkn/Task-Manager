package hexlet.code.app.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.dto.LabelDTO.LabelUpdateDTO;
import hexlet.code.app.mapper.LabelMapper;
import hexlet.code.app.model.Label;
import hexlet.code.app.repository.LabelRepository;
import hexlet.code.app.util.ModelGenerator;
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

import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LabelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private ModelGenerator modelGenerator;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;

    private Label testLabel;

    @BeforeEach
    public void setUp() {
        token = jwt().jwt(builder -> builder.subject("hexlet@example.com"));
    }

    @AfterEach
    public void cleanUp() {
        if (testLabel != null) {
            labelRepository.deleteById(testLabel.getId());
        }
    }

    private Label createLabelModel() {
        return Instancio.of(modelGenerator.getLabelModel()).create();
    }

    @Test
    public void testIndex() throws Exception {
        testLabel = createLabelModel();
        labelRepository.save(testLabel);

        var request = get("/api/labels").with(token);
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    public void testShow() throws Exception {
        testLabel = createLabelModel();
        labelRepository.save(testLabel);

        var request = get("/api/labels/{id}", testLabel.getId()).with(token);
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("id").isEqualTo(testLabel.getId()),
                v -> v.node("name").isEqualTo(testLabel.getName()),
                v -> v.node("createdAt").isEqualTo(testLabel.getCreatedAt().format(ModelGenerator.FORMATTER))
        );
    }

    @Test
    public void testUpdate() throws Exception {
        testLabel = createLabelModel();
        labelRepository.save(testLabel);

        var data = new LabelUpdateDTO();
        data.setName(JsonNullable.of("UPDATE"));


        var request = put("/api/labels/{id}", testLabel.getId()).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk());


        var updatedLabel = labelRepository.findById(testLabel.getId()).orElse(null);

        assertThat(updatedLabel).isNotNull();
        assertThat(updatedLabel.getName()).isEqualTo(data.getName().get());
    }

    @Test
    public void testCreate() throws Exception {
        var data = Map.of(
                "name", "To test create"
        );

        var request = post("/api/labels").with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        var result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        var body = result.getResponse().getContentAsString();


        var id = om.readTree(body).get("id").asLong();
        assertThat(labelRepository.findById(id)).isPresent();

        var addedLabel = labelRepository.findByName(data.get("name")).orElse(null);

        assertThatJson(body).isNotNull().and(
                json -> json.node("id").isEqualTo(addedLabel.getId()),
                json -> json.node("name").isEqualTo(addedLabel.getName()),
                json -> json.node("createdAt").isEqualTo(addedLabel.getCreatedAt().format(ModelGenerator.FORMATTER))
        );

    }

    @Test
    public void testDestroy() throws Exception {
        testLabel = createLabelModel();
        labelRepository.save(testLabel);

        var request = delete("/api/labels/{id}", testLabel.getId()).with(token);
        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        testLabel = labelRepository.findById(testLabel.getId()).orElse(null);
        assertThat(testLabel).isNull();
    }

    @Test
    public void testDestroyWithoutAuth() throws Exception {
        testLabel = createLabelModel();
        labelRepository.save(testLabel);

        var request = delete("/api/labels/{id}", testLabel.getId());
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }
}
