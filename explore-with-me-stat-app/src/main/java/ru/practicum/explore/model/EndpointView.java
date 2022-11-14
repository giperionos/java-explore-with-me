package ru.practicum.explore.model;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class EndpointView {
    private String app;
    private String uri;
    private Long hits;
}
