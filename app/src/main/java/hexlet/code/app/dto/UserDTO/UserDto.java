package hexlet.code.app.dto.UserDTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class UserDto {

    private Long id;
    private String firstname;
    private String lastname;
    private String email;
    private LocalDate createdAt;

}
