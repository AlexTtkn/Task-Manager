package hexlet.code.app.mapper;

import hexlet.code.app.dto.UserDTO.UserCreateDto;
import hexlet.code.app.dto.UserDTO.UserDto;
import hexlet.code.app.dto.UserDTO.UserUpdateDto;
import hexlet.code.app.model.User;
import org.mapstruct.*;

@Mapper(
        uses = {JsonNullableMapper.class, ReferenceMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class UserMapper {
    public abstract User map(UserCreateDto userCreateDto);

    public abstract UserDto map(User user);

    public abstract void update(UserUpdateDto data, @MappingTarget User model);
}
