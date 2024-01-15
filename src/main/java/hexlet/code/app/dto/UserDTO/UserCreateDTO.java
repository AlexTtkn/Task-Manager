package hexlet.code.app.dto.UserDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserCreateDTO {

    private String firstname;
    private String lastname;

    @NotBlank
    @Size(min = 3)
    private String passwordDigest;

    @Email
    private String email;

}
