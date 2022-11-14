package ru.practicum.explore.service.exceptions;

public class ParticipationRequestForRequesterNotFoundException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Для пользователя с id = %d запрос на участие с id = %d не найден в БД.";

    public ParticipationRequestForRequesterNotFoundException(Long userId, Long requestId) {
        super(String.format(ERROR_MESSAGE, userId, requestId));
    }
}
