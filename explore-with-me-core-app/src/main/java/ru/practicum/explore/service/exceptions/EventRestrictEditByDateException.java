package ru.practicum.explore.service.exceptions;

import ru.practicum.explore.model.Event;

public class EventRestrictEditByDateException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Дата и время мероприятия %s не может быть раньше, чем через два часа от текущего момента!";

    public EventRestrictEditByDateException(Event event) {
        super(String.format(ERROR_MESSAGE, event.getTitle()));
    }
}
