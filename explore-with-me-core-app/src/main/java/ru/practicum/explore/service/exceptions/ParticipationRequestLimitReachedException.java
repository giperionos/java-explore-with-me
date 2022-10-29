package ru.practicum.explore.service.exceptions;

import ru.practicum.explore.model.Event;
import ru.practicum.explore.model.User;

public class ParticipationRequestLimitReachedException extends RuntimeException {

    private static final String ERROR_MESSAGE = "%s, для мероприятия %s уже достигнут лимит заявок на участие.";

    public ParticipationRequestLimitReachedException(User user, Event event) {
        super(String.format(ERROR_MESSAGE, user.getName(), event.getTitle()));
    }
}
