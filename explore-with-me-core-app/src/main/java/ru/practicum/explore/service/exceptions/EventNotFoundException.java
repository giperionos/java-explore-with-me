package ru.practicum.explore.service.exceptions;

public class EventNotFoundException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Событие с id = %d не найдено в БД.";

    public EventNotFoundException(Long id) {
        super(String.format(ERROR_MESSAGE, id));
    }
}
