package ru.practicum.explore.service.exceptions;

public class CompilationNotFoundException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Подборка мероприятий с id = %d не найдена в БД.";

    public CompilationNotFoundException(Long compId) {
        super(String.format(ERROR_MESSAGE, compId));
    }
}
