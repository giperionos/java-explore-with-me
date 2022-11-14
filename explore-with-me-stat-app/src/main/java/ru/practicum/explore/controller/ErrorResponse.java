package ru.practicum.explore.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ErrorResponse {
    private String serverStatusCode;
    private String error;
}
