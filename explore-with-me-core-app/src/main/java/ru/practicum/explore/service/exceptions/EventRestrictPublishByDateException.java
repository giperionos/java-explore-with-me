package ru.practicum.explore.service.exceptions;

import ru.practicum.explore.model.Event;

public class EventRestrictPublishByDateException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Дата и время мероприятия %s должна быть не ранее чем за час от даты публикации!";

    public EventRestrictPublishByDateException(Event event) {
        super(String.format(ERROR_MESSAGE, event.getTitle()));
    }
}