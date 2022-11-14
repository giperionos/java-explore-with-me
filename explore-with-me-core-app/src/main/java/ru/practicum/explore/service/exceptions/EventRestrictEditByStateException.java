package ru.practicum.explore.service.exceptions;

import ru.practicum.explore.model.Event;

public class EventRestrictEditByStateException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Мероприятие %s не может быть изменено. Оно уже опубликовано!";

    public EventRestrictEditByStateException(Event event) {
        super(String.format(ERROR_MESSAGE, event.getTitle()));
    }
}
