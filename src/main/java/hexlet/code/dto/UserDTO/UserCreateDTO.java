package hexlet.code.dto.UserDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserCreateDTO {

    private String firstName;
    private String lastName;

    @NotBlank
    @Size(min = 3)
    private String password;

    @Email
    private String email;

}
