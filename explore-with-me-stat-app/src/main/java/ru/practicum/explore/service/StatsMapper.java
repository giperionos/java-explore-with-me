package ru.practicum.explore.service;

import ru.practicum.explore.dto.EndpointHitDto;
import ru.practicum.explore.dto.EndpointViewDto;
import ru.practicum.explore.model.EndpointEntity;
import ru.practicum.explore.model.EndpointView;

import java.time.LocalDateTime;
import static ru.practicum.explore.config.Config.formatter;

public class StatsMapper {
    public static EndpointEntity toEndpointEntity(EndpointHitDto endpointHitDto) {
        return new EndpointEntity(
                endpointHitDto.getId(),
                endpointHitDto.getApp(),
                endpointHitDto.getUri(),
                endpointHitDto.getIp(),
                endpointHitDto.getTimestamp() == null ? null : LocalDateTime.parse(endpointHitDto.getTimestamp(), formatter)
        );
    }

    public static EndpointHitDto toEndpointHitDto(EndpointEntity endpoint) {
        return new EndpointHitDto(
                endpoint.getId(),
                endpoint.getApp(),
                endpoint.getUri(),
                endpoint.getIp(),
                endpoint.getCreationDate() == null ? null : endpoint.getCreationDate().format(formatter)
        );
    }

    public static EndpointViewDto toEndpointViewDto(EndpointView endpointView) {
        return new EndpointViewDto(endpointView.getApp(), endpointView.getUri(), endpointView.getHits());
    }
}
