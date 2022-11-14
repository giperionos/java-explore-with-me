package ru.practicum.explore.service.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.explore.dto.*;
import ru.practicum.explore.model.Category;
import ru.practicum.explore.model.Event;
import ru.practicum.explore.model.ParticipationRequest;
import ru.practicum.explore.model.User;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventMapper {

    private static final String URI_PATTERN = "/events/%s";

    public static Event toEvent(NewEventDto newEventDto, Category category, User initiator) {
        Event event = new Event();
        event.setTitle(newEventDto.getTitle());
        event.setEventDate(newEventDto.getEventDate());
        event.setAnnotation(newEventDto.getAnnotation());
        event.setCategory(category);
        event.setDescription(newEventDto.getDescription());
        event.setLat(newEventDto.getLocation().getLat());
        event.setLon(newEventDto.getLocation().getLon());
        event.setPaid(newEventDto.getPaid());
        event.setParticipantLimit(newEventDto.getParticipantLimit());
        event.setRequestModeration(newEventDto.getRequestModeration());
        event.setState(EventState.PENDING);
        event.setInitiator(initiator);
        return event;
    }

    public static EventFullDto toEventFullDto(Event event, Long confirmedRequests, Long views) {
        EventFullDto eventFullDto = new EventFullDto();
        eventFullDto.setAnnotation(event.getAnnotation());
        eventFullDto.setCategory(CategoryMapper.toCategoryDto(event.getCategory()));
        eventFullDto.setConfirmedRequests(confirmedRequests);
        eventFullDto.setCreatedOn(event.getCreationDate());
        eventFullDto.setDescription(event.getDescription());
        eventFullDto.setEventDate(event.getEventDate());
        eventFullDto.setId(event.getId());
        eventFullDto.setInitiator(UserMapper.toUserShortDto(event.getInitiator()));
        eventFullDto.setLocation(new LocationDto(event.getLat(), event.getLon()));
        eventFullDto.setPaid(event.isPaid());
        eventFullDto.setParticipantLimit(event.getParticipantLimit());
        eventFullDto.setPublishedOn(event.getPublishDate());
        eventFullDto.setRequestModeration(event.isRequestModeration());
        eventFullDto.setState(event.getState());
        eventFullDto.setTitle(event.getTitle());
        eventFullDto.setViews(views);
        return eventFullDto;
    }

    public static EventFullDto toEventFullDtoWithoutViewsAndRequests(Event event) {
        EventFullDto eventFullDto = new EventFullDto();
        eventFullDto.setAnnotation(event.getAnnotation());
        eventFullDto.setCategory(CategoryMapper.toCategoryDto(event.getCategory()));
        eventFullDto.setCreatedOn(event.getCreationDate());
        eventFullDto.setDescription(event.getDescription());
        eventFullDto.setEventDate(event.getEventDate());
        eventFullDto.setId(event.getId());
        eventFullDto.setInitiator(UserMapper.toUserShortDto(event.getInitiator()));
        eventFullDto.setLocation(new LocationDto(event.getLat(), event.getLon()));
        eventFullDto.setPaid(event.isPaid());
        eventFullDto.setParticipantLimit(event.getParticipantLimit());
        eventFullDto.setPublishedOn(event.getPublishDate());
        eventFullDto.setRequestModeration(event.isRequestModeration());
        eventFullDto.setState(event.getState());
        eventFullDto.setTitle(event.getTitle());
        return eventFullDto;
    }

    public static EventMiniDto toEventMiniDto(Event event) {
        EventMiniDto eventMiniDto = new EventMiniDto();
        eventMiniDto.setId(event.getId());
        eventMiniDto.setEventDate(event.getEventDate());
        eventMiniDto.setTitle(event.getTitle());
        return eventMiniDto;
    }

    public static List<EventFullDto> toEventsFullDto(List<Event> events, List<ParticipationRequest> requestsForEvents, List<EndpointViewDto> statisticsForEvents) {
        List<EventFullDto> result = new ArrayList<>();

        for (Event event: events) {
            //заполнить основную информацию в eventFullDto из Event
            EventFullDto eventFullDto = toEventFullDtoWithoutViewsAndRequests(event);

            //заполнить подтвержденные заявки
            for (ParticipationRequest request: requestsForEvents) {
                if (request.getEvent().getId().equals(event.getId())) {
                    eventFullDto.setConfirmedRequests(eventFullDto.getConfirmedRequests() + 1L);
                }
            }

            //заполнить просмотры
            for (EndpointViewDto endpointViewDto: statisticsForEvents) {
                //в endpointViewDto в uri хранится строка вида "/events/9"
                //Можно сформировать для текущего event такую же строку, чтобы удобнее сравнивать
                if (endpointViewDto.getUri().equals(String.format(URI_PATTERN, event.getId()))) {
                    eventFullDto.setViews(endpointViewDto.getHits());
                }
            }

            //добавить в результат
            result.add(eventFullDto);
        }

        return result;
    }

    public static EventShortDto toEventShortDtoFromFull(EventFullDto eventFullDto) {
        EventShortDto shortDto = new EventShortDto();
        shortDto.setAnnotation(eventFullDto.getAnnotation());
        shortDto.setCategory(eventFullDto.getCategory());
        shortDto.setConfirmedRequests(eventFullDto.getConfirmedRequests());
        shortDto.setEventDate(eventFullDto.getEventDate());
        shortDto.setId(eventFullDto.getId());
        shortDto.setInitiator(eventFullDto.getInitiator());
        shortDto.setPaid(eventFullDto.getPaid());
        shortDto.setTitle(eventFullDto.getTitle());
        shortDto.setViews(eventFullDto.getViews());
        return shortDto;
    }
}
