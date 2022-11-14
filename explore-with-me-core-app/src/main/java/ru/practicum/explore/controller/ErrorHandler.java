package ru.practicum.explore.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.explore.dto.ApiErrorDto;
import ru.practicum.explore.service.exceptions.*;
import ru.practicum.explore.validator.EndDateBeforeStartDateException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class,
            MissingServletRequestParameterException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorDto handleValidateArgumentException(Exception exception) {
        log.info("{}: {}", HttpStatus.BAD_REQUEST.value(), exception.getMessage(), exception);
        return fillApiErrorDto(exception, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({EndDateBeforeStartDateException.class,
            SortTypeNotValidException.class,
            EventStateTypeNotValidException.class,
            AdminChatActionNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorDto handleValidateRuntimeException(RuntimeException exception) {
        log.info("{}: {}", HttpStatus.BAD_REQUEST.value(), exception.getMessage(), exception);
        return fillApiErrorDto(exception, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({CategoryNotFoundException.class,
            EventNotFoundException.class,
            ParticipationRequestForRequesterNotFoundException.class,
            ParticipationRequestNotFoundException.class,
            UserNotFoundException.class,
            CompilationNotFoundException.class,
            CompilationNotContainEventForDeleteException.class,
            ChatNotFoundException.class,
            ChatForRequestNotFoundException.class,
            MessageNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorDto handleNotFoundExceptionException(RuntimeException exception) {
        log.info("{}: {}", HttpStatus.NOT_FOUND.value(), exception.getMessage(), exception);
        return fillApiErrorDto(exception, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({CategoryRestrictDeleteException.class,
            ParticipationRequestAlreadyExistsException.class,
            ParticipationRequestLimitReachedException.class,
            ParticipationRequestOnNotPublishedEvent.class,
            ParticipationRequestUserOwnEventException.class,
            UserHaveNoAccessEventException.class,
            EventRestrictEditByStateException.class,
            EventRestrictEditByDateException.class,
            EventRestrictAddByDateException.class,
            EventRestrictPublishByDateException.class,
            EventRestrictPublishByStateException.class,
            EventRestrictRejectByStateException.class,
            CompilationAlreadyExistsEventException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiErrorDto handleForbiddenException(RuntimeException exception) {
        log.info("{}: {}", HttpStatus.FORBIDDEN.value(), exception.getMessage(), exception);
        return fillApiErrorDto(exception, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorDto handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
        log.info("{}: {}", HttpStatus.CONFLICT.value(), exception.getMessage(), exception);
        return fillApiErrorDto(exception, HttpStatus.CONFLICT);
    }

    @ExceptionHandler({Exception.class, UnexpectedException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorDto handleCommonException(Exception exception) {
        log.info("{}: {}", HttpStatus.INTERNAL_SERVER_ERROR.value(), exception.getMessage(), exception);
        return fillApiErrorDto(exception, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ApiErrorDto fillApiErrorDto(Exception exception, HttpStatus httpStatus) {
        return ApiErrorDto.builder()
                .errors(Arrays.stream(exception.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.toList()))
                .message(exception.getMessage())
                .reason(exception.getCause() == null ? null : exception.getCause().getMessage())
                .status(httpStatus)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
