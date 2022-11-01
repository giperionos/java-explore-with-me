package ru.practicum.explore.service;

import ru.practicum.explore.dto.ParticipationRequestDto;

import java.util.List;

public interface ParticipationRequestService {
    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto addParticipationRequestByUserForEvent(Long userId, Long eventId);

    ParticipationRequestDto cancelParticipationRequestByUser(Long userId, Long requestId);

    List<ParticipationRequestDto> getParticipationRequestsInEventOfCurrentUser(Long userId, Long eventId);

    ParticipationRequestDto confirmParticipationRequestInEventOfCurrentUser(Long userId, Long eventId, Long requestId);

    ParticipationRequestDto rejectParticipationRequestInEventOfCurrentUser(Long userId, Long eventId, Long requestId);
}
