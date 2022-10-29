package ru.practicum.explore.validator;

import java.time.LocalDateTime;

public class StartDateBeforeEndDateValidator {
    public static void validate(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start)) {
            throw new EndDateBeforeStartDateException(
                    String.format("Дата окончания %s впереди даты старта %s", end, start)
            );
        }
    }
}
