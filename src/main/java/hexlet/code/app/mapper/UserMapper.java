package hexlet.code.app.mapper;

import hexlet.code.app.dto.UserDTO.UserCreateDTO;
import hexlet.code.app.dto.UserDTO.UserDTO;
import hexlet.code.app.dto.UserDTO.UserUpdateDTO;
import hexlet.code.app.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        uses = {JsonNullableMapper.class, ReferenceMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class UserMapper {

    public abstract User map(UserCreateDTO userCreateDto);

    public abstract UserDTO map(User user);

    public abstract void update(UserUpdateDTO data, @MappingTarget User model);

}
