package ru.practicum.explore.service.exceptions;

public class ChatAccessException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Пользователь с id = %d не имеет доступ к чату с id = %d";

    public ChatAccessException(Long userId, Long chatId) {
        super(String.format(ERROR_MESSAGE, userId, chatId));
    }
}
