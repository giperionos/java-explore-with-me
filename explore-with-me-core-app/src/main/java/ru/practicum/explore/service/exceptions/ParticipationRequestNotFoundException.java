package ru.practicum.explore.service.exceptions;

public class ParticipationRequestNotFoundException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Запрос на участие с id = %d не найден в БД.";

    public ParticipationRequestNotFoundException(Long requestId) {
        super(String.format(ERROR_MESSAGE, requestId));
    }
}
