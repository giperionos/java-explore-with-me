package ru.practicum.explore.dto;

import ru.practicum.explore.service.exceptions.SortTypeNotValidException;

public enum EventSortType {
    EVENT_DATE,
    VIEWS;

    public static EventSortType from(String sortStr) {
        for (EventSortType sortType: values()) {
            if (sortType.name().equalsIgnoreCase(sortStr)) {
                return sortType;
            }
        }

        throw new SortTypeNotValidException(sortStr);
    }
}
