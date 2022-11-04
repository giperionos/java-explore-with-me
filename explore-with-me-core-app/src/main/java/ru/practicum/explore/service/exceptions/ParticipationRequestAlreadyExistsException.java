package ru.practicum.explore.service.exceptions;

public class ParticipationRequestAlreadyExistsException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Пользователь с id = %d, вы уже подавали заявку для участия в мероприятии  с id = %d.";

    public ParticipationRequestAlreadyExistsException(Long userId, Long eventId) {
        super(String.format(ERROR_MESSAGE, userId, eventId));
    }
}
