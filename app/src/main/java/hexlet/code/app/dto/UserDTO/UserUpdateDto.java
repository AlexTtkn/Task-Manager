package hexlet.code.app.dto.UserDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Setter
@Getter
public class UserUpdateDto {

    @NotNull
    @Size(min = 3)
    private JsonNullable<String> password;

    @NotNull
    @Email
    private JsonNullable<String> email;
}
