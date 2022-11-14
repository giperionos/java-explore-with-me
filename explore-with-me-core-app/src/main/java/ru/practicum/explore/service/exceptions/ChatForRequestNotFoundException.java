package ru.practicum.explore.service.exceptions;

public class ChatForRequestNotFoundException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Не найден чат для запроса на участие с id = %d";

    public ChatForRequestNotFoundException(Long id) {
        super(String.format(ERROR_MESSAGE, id));
    }
}
