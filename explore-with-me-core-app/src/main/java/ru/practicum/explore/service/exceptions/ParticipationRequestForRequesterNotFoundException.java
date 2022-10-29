package ru.practicum.explore.service.exceptions;

import ru.practicum.explore.model.User;

public class ParticipationRequestForRequesterNotFoundException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Для пользователя %s запрос на участие с id = %d не найден в БД.";

    public ParticipationRequestForRequesterNotFoundException(User user, Long requestId) {
        super(String.format(ERROR_MESSAGE, user.getName(), requestId));
    }
}
