package ru.practicum.explore.dto;

import ru.practicum.explore.service.exceptions.AdminChatActionNotValidException;

public enum AdminChatAction {
    OPEN,
    CLOSE;

    public static AdminChatAction from(String actionStr) {
        for (AdminChatAction action: values()) {
            if (action.name().equalsIgnoreCase(actionStr)) {
                return action;
            }
        }

        throw new AdminChatActionNotValidException(actionStr);
    }
}
