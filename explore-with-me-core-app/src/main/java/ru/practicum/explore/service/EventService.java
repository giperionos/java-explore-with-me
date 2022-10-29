package ru.practicum.explore.service;

import ru.practicum.explore.dto.*;
import ru.practicum.explore.model.Event;
import ru.practicum.explore.model.ParticipationRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface EventService {
    List<EventShortDto> getEventsByFilter(UserEventFilter filter, HttpServletRequest request);

    EventFullDto getEventWithFullInfoById(Long eventId, HttpServletRequest request);

    List<EventShortDto> getEventsAddedByCurrentUser(Long userId, Integer from, Integer size);

    EventFullDto updateEventAddedByCurrentUser(Long userId, UpdateEventRequestDto updateEventRequestDto);

    EventFullDto addNewEventByCurrentUser(Long userId, NewEventDto newEventDto);

    EventFullDto getFullInfoEventAddedByCurrentUser(Long userId, Long eventId);

    EventFullDto cancelEventAddedByCurrentUser(Long userId, Long eventId);

    List<EventFullDto> getEventsWithFullInfoByFilter(AdminEventFilter filter);

    EventFullDto editEvent(Long eventId, AdminUpdateEventRequestDto adminUpdateEventRequestDto);

    EventFullDto publishEvent(Long eventId);

    EventFullDto rejectEvent(Long eventId);

    Event findEventByIdOrThrowException(Long eventId);

    EndpointHitDto doHitRequest(HttpServletRequest request);

    List<Event> findAllByIdsAndState(List<Long> eventIds, EventState state);

    List<ParticipationRequest> findAllRequestsByEventIdsAndStatus(List<Long> eventIds, RequestStatus status);

    EventShortDto mapEventToEventShortDto(Event event);
}
