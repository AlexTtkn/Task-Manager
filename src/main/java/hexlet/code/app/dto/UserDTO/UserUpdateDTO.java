package hexlet.code.app.dto.UserDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Setter
@Getter
public class UserUpdateDTO {

    @NotBlank
    @Size(min = 3)
    private JsonNullable<String> passwordDigest;

    private JsonNullable<String> firstname;

    private JsonNullable<String> lastname;

    @Email
    private JsonNullable<String> email;
}
