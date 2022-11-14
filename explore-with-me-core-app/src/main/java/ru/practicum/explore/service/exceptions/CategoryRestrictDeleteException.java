package ru.practicum.explore.service.exceptions;

public class CategoryRestrictDeleteException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Категория с id = %d не может быть удалена. Есть связанные с ней события.";

    public CategoryRestrictDeleteException(Long id) {
        super(String.format(ERROR_MESSAGE, id));
    }
}
