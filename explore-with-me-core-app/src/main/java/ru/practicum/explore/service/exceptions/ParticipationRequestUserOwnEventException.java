package ru.practicum.explore.service.exceptions;

import ru.practicum.explore.model.Event;
import ru.practicum.explore.model.User;

public class ParticipationRequestUserOwnEventException extends RuntimeException {

    private static final String ERROR_MESSAGE = "%s, вы пытаетесь сделать запрос на участие в своем же мероприятии %s";

    public ParticipationRequestUserOwnEventException(User user, Event event) {
        super(String.format(ERROR_MESSAGE, user.getName(), event.getTitle()));
    }
}
