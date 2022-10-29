package ru.practicum.explore.service.exceptions;

import ru.practicum.explore.dto.EventState;

import java.util.Arrays;

public class EventStateTypeNotValidException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Полученный тип статуса мероприятия %s не соответствует ни одному из ожидаемых: %s";

    public EventStateTypeNotValidException(String stateStr) {
        super(String.format(ERROR_MESSAGE, stateStr, Arrays.toString(EventState.values())));
    }
}
