package ru.practicum.explore.service.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.explore.dto.ParticipationRequestDto;
import ru.practicum.explore.model.ParticipationRequest;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestMapper {
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
