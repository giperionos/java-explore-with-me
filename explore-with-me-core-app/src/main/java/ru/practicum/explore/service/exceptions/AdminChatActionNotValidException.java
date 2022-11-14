package ru.practicum.explore.service.exceptions;

import ru.practicum.explore.dto.AdminChatAction;

import java.util.Arrays;

public class AdminChatActionNotValidException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Полученный тип действия %s не соответствует ни одному из ожидаемых: %s";

    public AdminChatActionNotValidException(String action) {
        super(String.format(ERROR_MESSAGE, action, Arrays.toString(AdminChatAction.values())));
    }
}
