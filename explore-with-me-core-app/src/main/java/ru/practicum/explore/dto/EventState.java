package ru.practicum.explore.dto;

import ru.practicum.explore.service.exceptions.EventStateTypeNotValidException;

public enum EventState {
    PENDING,
    PUBLISHED,
    CANCELED;

    public static EventState from(String stateStr) {
        for (EventState eventState: values()) {
            if (eventState.name().equalsIgnoreCase(stateStr)) {
                return eventState;
            }
        }

        throw new EventStateTypeNotValidException(stateStr);
    }
}
