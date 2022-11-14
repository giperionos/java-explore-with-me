package ru.practicum.explore.service.exceptions;

import ru.practicum.explore.model.Event;

public class EventRestrictPublishByStateException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Мероприятие %s не может быть опубликовано. Оно в статусе: %s";

    public EventRestrictPublishByStateException(Event event) {
        super(String.format(ERROR_MESSAGE, event.getTitle(), event.getState()));
    }
}
