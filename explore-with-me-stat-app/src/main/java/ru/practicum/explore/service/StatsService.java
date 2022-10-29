package ru.practicum.explore.service;

import ru.practicum.explore.EndpointViewParamsHelper;
import ru.practicum.explore.dto.EndpointHitDto;
import ru.practicum.explore.dto.EndpointViewDto;

import java.util.List;

public interface StatsService {
    EndpointHitDto addHitToLog(EndpointHitDto endpointHitDto);

    List<EndpointViewDto> getEndpointStatsByParams(EndpointViewParamsHelper params);
}
