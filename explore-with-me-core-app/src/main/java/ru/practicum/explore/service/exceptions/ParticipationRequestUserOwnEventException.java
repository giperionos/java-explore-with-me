package ru.practicum.explore.service.exceptions;

import ru.practicum.explore.model.Event;

public class ParticipationRequestUserOwnEventException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Пользователя с id = %d, вы пытаетесь сделать запрос на участие в своем же мероприятии %s";

    public ParticipationRequestUserOwnEventException(Long userId, Event event) {
        super(String.format(ERROR_MESSAGE, userId, event.getTitle()));
    }
}
