package ru.practicum.explore.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EndpointViewDto {
    private String app;
    private String uri;
    private Long hits;
}
