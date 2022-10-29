package ru.practicum.explore.service.exceptions;

import ru.practicum.explore.model.Event;

public class EventRestrictRejectByStateException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Мероприятие %s не может быть отклонено. Оно в статусе: %s";

    public EventRestrictRejectByStateException(Event event) {
        super(String.format(ERROR_MESSAGE, event.getTitle(), event.getState()));
    }
}
