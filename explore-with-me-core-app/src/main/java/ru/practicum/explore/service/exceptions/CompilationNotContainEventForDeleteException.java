package ru.practicum.explore.service.exceptions;

import ru.practicum.explore.model.Compilation;
import ru.practicum.explore.model.Event;

public class CompilationNotContainEventForDeleteException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Подборка мероприятий %s не содержит в себе мероприятие %s,"
            + "  которое требуется удалить.";

    public CompilationNotContainEventForDeleteException(Compilation compilation, Event event) {
        super(String.format(ERROR_MESSAGE, compilation.getTitle(), event.getTitle()));
    }
}
