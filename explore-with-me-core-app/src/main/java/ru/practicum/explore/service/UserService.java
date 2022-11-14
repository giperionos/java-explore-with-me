package ru.practicum.explore.service;

import ru.practicum.explore.dto.NewUserRequest;
import ru.practicum.explore.dto.UserDto;
import ru.practicum.explore.dto.UserFilter;

import java.util.List;

public interface UserService {
    UserDto addNewUser(NewUserRequest newUserRequest);

    List<UserDto> getUsersByFilter(UserFilter filter);

    void deleteUserById(Long userId);
}
