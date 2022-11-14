package ru.practicum.explore.service.exceptions;

import ru.practicum.explore.model.Event;

public class ParticipationRequestLimitReachedException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Для мероприятия %s уже достигнут лимит заявок на участие.";

    public ParticipationRequestLimitReachedException(Event event) {
        super(String.format(ERROR_MESSAGE, event.getTitle()));
    }
}
