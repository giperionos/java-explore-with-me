package ru.practicum.explore.service.exceptions;

public class MessageNotFoundException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Сообщение с id = %d не найдено в БД.";

    public MessageNotFoundException(Long messId) {
        super(String.format(ERROR_MESSAGE, messId));
    }
}
