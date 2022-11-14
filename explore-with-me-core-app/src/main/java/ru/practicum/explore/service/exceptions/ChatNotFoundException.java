package ru.practicum.explore.service.exceptions;

public class ChatNotFoundException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Чат с id = %d не найден в БД.";

    public ChatNotFoundException(Long id) {
        super(String.format(ERROR_MESSAGE, id));
    }
}
