package hexlet.code.dto.TaskDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TaskCreateDTO {

    private Long index;

    @NotNull
    @JsonProperty("assignee_id")
    private Long assigneeId;

    @NotBlank
    private String title;

    private String content;

    @NotNull
    private String status;

    private List<Long> taskLabelIds;

}
