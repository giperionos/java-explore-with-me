package ru.practicum.explore.service.exceptions;

public class UserNotFoundException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Пользователь с id = %d не найден в БД.";

    public UserNotFoundException(Long id) {
        super(String.format(ERROR_MESSAGE, id));
    }
}
