package ru.practicum.explore.service.mapper;

import ru.practicum.explore.dto.ParticipationRequestDto;
import ru.practicum.explore.model.ParticipationRequest;

public class RequestMapper {
    public static ParticipationRequestDto toParticipationRequestDto(ParticipationRequest request) {
        return new ParticipationRequestDto(
                request.getId(),
                request.getEvent().getId(),
                request.getRequester().getId(),
                request.getStatus(),
                request.getCreated()
        );
    }
}
