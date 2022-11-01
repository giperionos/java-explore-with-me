package ru.practicum.explore.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.dto.NewUserRequest;
import ru.practicum.explore.dto.UserDto;
import ru.practicum.explore.dto.UserFilter;
import ru.practicum.explore.service.UserService;

import java.util.List;

@RestController
@RequestMapping(path = "/admin/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AdminUserController {

    private final UserService service;

    @PostMapping
    public UserDto addNewUser(@Validated @RequestBody NewUserRequest newUserRequest) {
        log.info("AdminUserController: Получен {} запрос с параметрами: newUserRequest = {}", "POST", newUserRequest);
        return service.addNewUser(newUserRequest);
    }

    @GetMapping
    public List<UserDto> getUsersByParams(
            @RequestParam(name = "ids", required = false) List<Long> usersIds,
            @RequestParam(name = "from", defaultValue = "0")  Integer from,
            @RequestParam(name = "size", defaultValue = "10")  Integer size) {

        log.info("AdminUserController: Получен GET запрос с параметрами: "
                + "ids = {}"
                + "from = {}"
                + "size = {}",
                usersIds,
                from,
                size);

        UserFilter filter = UserFilter.builder()
                .userIds(usersIds)
                .from(from)
                .size(size)
                .build();

        return service.getUsersByFilter(filter);
    }

    @DeleteMapping("/{userId}")
    public void deleteUserById(@PathVariable Long userId) {
        log.info("AdminUserController: Получен {} запрос с параметрами: userId = {}", "DELETE /{userId}", userId);
        service.deleteUserById(userId);
    }
}
