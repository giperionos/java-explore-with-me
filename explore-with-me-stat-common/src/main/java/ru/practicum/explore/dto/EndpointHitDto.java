package ru.practicum.explore.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EndpointHitDto {

    private Long id;

    @NotBlank(message = "Не указано название сервиса.")
    private String app;

    @NotBlank(message = "Не указан URI сервиса.")
    private String uri;

    @NotBlank(message = "Не указан IP пользователя.")
    private String ip;

    private String timestamp;
}
