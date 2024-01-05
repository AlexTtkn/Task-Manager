package hexlet.code.app.mapper;

import hexlet.code.app.dto.UserDTO.UserCreateDto;
import hexlet.code.app.dto.UserDTO.UserDto;
import hexlet.code.app.dto.UserDTO.UserUpdateDto;
import hexlet.code.app.model.User;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.MappingTarget;
import org.mapstruct.BeforeMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(
        uses = {JsonNullableMapper.class, ReferenceMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class UserMapper {

    @Autowired
    private PasswordEncoder passwordEncoder;

    public abstract User map(UserCreateDto userCreateDto);

    public abstract UserDto map(User user);

    public abstract void update(UserUpdateDto data, @MappingTarget User model);

    @BeforeMapping
    public void encryptPassword(UserCreateDto data) {
        var password = data.getPasswordDigest();
        data.setPasswordDigest(passwordEncoder.encode(password));
    }
}
