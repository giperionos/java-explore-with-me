package ru.practicum.explore.service.exceptions;

import ru.practicum.explore.dto.EventSortType;
import java.util.Arrays;

public class SortTypeNotValidException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Полученный тип сортировки %s не соответствует ни одному из ожидаемых: %s";

    public SortTypeNotValidException(String sortTypeStr) {
        super(String.format(ERROR_MESSAGE, sortTypeStr, Arrays.toString(EventSortType.values())));
    }
}
