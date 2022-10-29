package ru.practicum.explore.service.exceptions;

import ru.practicum.explore.model.Event;
import ru.practicum.explore.model.User;

public class ParticipationRequestAlreadyExistsException extends RuntimeException {

    private static final String ERROR_MESSAGE = "%s, вы уже подавали заявку для участия в мероприятии %s.";

    public ParticipationRequestAlreadyExistsException(User user, Event event) {
        super(String.format(ERROR_MESSAGE, user.getName(), event.getTitle()));
    }
}
