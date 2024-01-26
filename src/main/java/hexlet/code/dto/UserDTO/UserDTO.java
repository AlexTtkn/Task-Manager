package hexlet.code.dto.UserDTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class UserDTO {

    private Long id;
    private String firstname;
    private String lastname;
    private String email;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdAt;

}
