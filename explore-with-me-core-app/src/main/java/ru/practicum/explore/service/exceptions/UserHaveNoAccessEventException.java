package ru.practicum.explore.service.exceptions;

import ru.practicum.explore.model.Event;
import ru.practicum.explore.model.User;

public class UserHaveNoAccessEventException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Пользователь %s пытается получить доступ к не своему мероприятию %s.";

    public UserHaveNoAccessEventException(User user, Event event) {
        super(String.format(ERROR_MESSAGE, user.getName(), event.getTitle()));
    }
}
