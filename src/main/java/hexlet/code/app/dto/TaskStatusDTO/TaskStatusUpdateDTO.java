package hexlet.code.app.dto.TaskStatusDTO;

import jakarta.validation.constraints.NotBlank;
import org.openapitools.jackson.nullable.JsonNullable;

public class TaskStatusUpdateDTO {

    @NotBlank
    private JsonNullable<String> name;

}
