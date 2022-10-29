package ru.practicum.explore.service.exceptions;

public class CategoryNotFoundException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Категория с id = %d не найдена в БД.";

    public CategoryNotFoundException(Long id) {
        super(String.format(ERROR_MESSAGE, id));
    }
}
