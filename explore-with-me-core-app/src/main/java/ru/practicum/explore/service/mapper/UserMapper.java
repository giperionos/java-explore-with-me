package ru.practicum.explore.service.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.explore.dto.NewUserRequest;
import ru.practicum.explore.dto.UserDto;
import ru.practicum.explore.dto.UserShortDto;
import ru.practicum.explore.model.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserMapper {
    public static UserDto toUserDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail());
    }

    public static User toUser(UserDto userDto) {
        return new User(userDto.getId(), userDto.getName(), userDto.getEmail());
    }

    public static User toUser(NewUserRequest newUserRequest) {
        User user = new User();
        user.setName(newUserRequest.getName());
        user.setEmail(newUserRequest.getEmail());
        return user;
    }

    public static UserShortDto toUserShortDto(User user) {
        return new UserShortDto(user.getId(), user.getName());
    }
}
