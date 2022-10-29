package ru.practicum.explore.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.explore.EndpointViewParamsHelper;
import ru.practicum.explore.dto.EndpointHitDto;
import ru.practicum.explore.dto.EndpointViewDto;
import ru.practicum.explore.repository.EndpointEntityRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final EndpointEntityRepository repository;

    @Override
    public EndpointHitDto addHitToLog(EndpointHitDto endpointHitDto) {
        return StatsMapper.toEndpointHitDto(repository.save(StatsMapper.toEndpointEntity(endpointHitDto)));
    }

    @Override
    public List<EndpointViewDto> getEndpointStatsByParams(EndpointViewParamsHelper params) {

        if (params.getUnique() && params.getUris() != null && params.getUris().size() > 0) {
            return repository.getEndpointStatsByDateAndUniqueIpAndUri(
                    params.getStart(), params.getEnd(), params.getUris())
                    .stream()
                    .map(StatsMapper::toEndpointViewDto)
                    .collect(Collectors.toList());
        }

        if (params.getUnique() && (params.getUris() == null || params.getUris().size() == 0)) {
            return repository.getEndpointStatsByDateAndUniqueIp(
                     params.getStart(), params.getEnd())
                    .stream()
                    .map(StatsMapper::toEndpointViewDto)
                    .collect(Collectors.toList());
        }

        if (!params.getUnique() && params.getUris() != null && params.getUris().size() > 0) {
            return repository.getEndpointStatsByDateAndUri(
                    params.getStart(), params.getEnd(), params.getUris())
                    .stream()
                    .map(StatsMapper::toEndpointViewDto)
                    .collect(Collectors.toList());
        }

        if (!params.getUnique() && (params.getUris() == null || params.getUris().size() == 0)) {
            return repository.getEndpointStatsByDate(
                    params.getStart(), params.getEnd())
                    .stream()
                    .map(StatsMapper::toEndpointViewDto)
                    .collect(Collectors.toList());
        }

        return null;
    }
}
