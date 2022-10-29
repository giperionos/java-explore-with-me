package ru.practicum.explore.service.exceptions;

import ru.practicum.explore.model.Compilation;
import ru.practicum.explore.model.Event;

public class CompilationAlreadyExistsEventException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Подборка мероприятий %s уже содержит в себе мероприятие %s,"
            + "  которое требуется добавить.";

    public CompilationAlreadyExistsEventException(Compilation compilation, Event event) {
        super(String.format(ERROR_MESSAGE, compilation.getTitle(), event.getTitle()));
    }
}
