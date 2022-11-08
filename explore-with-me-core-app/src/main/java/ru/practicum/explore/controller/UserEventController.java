package ru.practicum.explore.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.dto.*;
import ru.practicum.explore.service.ChatService;
import ru.practicum.explore.service.EventService;
import ru.practicum.explore.service.ParticipationRequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserEventController {

    private final EventService eventService;
    private final ParticipationRequestService requestService;
    private final ChatService chatService;

    @GetMapping("/{userId}/events")
    public List<EventShortDto> getEventsAddedByCurrentUser(
            @PathVariable Long userId,
            @RequestParam(name = "from", defaultValue = "0")  Integer from,
            @RequestParam(name = "size", defaultValue = "10")  Integer size) {

        log.info("UserEventController: Получен {} запрос с параметрами: userId = {} from = {} size = {}",
                "GET /{userId}/events", userId, from, size);

        return eventService.getEventsAddedByCurrentUser(userId, from, size);
    }

    @PatchMapping("/{userId}/events")
    public EventFullDto updateEventAddedByCurrentUser(
            @PathVariable Long userId,
            @Validated @RequestBody UpdateEventRequestDto updateEventRequestDto) {

        log.info("UserEventController: Получен {} запрос с параметрами: userId = {} updateEventRequestDto = {}",
                "PATCH /{userId}/events", userId, updateEventRequestDto);

        return eventService.updateEventAddedByCurrentUser(userId, updateEventRequestDto);
    }

    @PostMapping("/{userId}/events")
    public EventFullDto addNewEventByCurrentUser(
            @PathVariable Long userId,
            @Validated @RequestBody NewEventDto newEventDto) {

        log.info("UserEventController: Получен {} запрос с параметрами: userId = {} newEventDto = {}",
                "POST /{userId}/events", userId, newEventDto);

        return eventService.addNewEventByCurrentUser(userId, newEventDto);
    }

    @GetMapping("/{userId}/events/{eventId}")
    public EventFullDto getFullInfoEventAddedByCurrentUser(@PathVariable Long userId, @PathVariable Long eventId) {

        log.info("UserEventController: Получен {} запрос с параметрами: userId = {} eventId = {}",
                "GET /{userId}/events/{eventId}", userId, eventId);

        return eventService.getFullInfoEventAddedByCurrentUser(userId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}")
    public EventFullDto cancelEventAddedByCurrentUser(@PathVariable Long userId, @PathVariable Long eventId) {

        log.info("UserEventController: Получен {} запрос с параметрами: userId = {} eventId = {}",
                "PATCH /{userId}/events/{eventId}", userId, eventId);
        return eventService.cancelEventAddedByCurrentUser(userId, eventId);
        //Обратите внимание: Отменить можно только событие в состоянии ожидания модерации.
    }

    @GetMapping("/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> getParticipationRequestsInEventOfCurrentUser(
            @PathVariable Long userId,
            @PathVariable Long eventId) {

        log.info("UserEventController: Получен {} запрос с параметрами: userId = {} eventId = {}",
                "GET /{userId}/events/{eventId}/requests", userId, eventId);

        return requestService.getParticipationRequestsInEventOfCurrentUser(userId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}/requests/{reqId}/confirm")
    public ParticipationRequestDto confirmParticipationRequestInEventOfCurrentUser(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @PathVariable(name = "reqId") Long requestId) {

        log.info("UserEventController: Получен {} запрос с параметрами: userId = {} eventId = {} requestId = {}",
                "PATCH /{userId}/events/{eventId}/requests/{reqId}/confirm", userId, eventId, requestId);

        return requestService.confirmParticipationRequestInEventOfCurrentUser(userId, eventId, requestId);
    }

    @PatchMapping("/{userId}/events/{eventId}/requests/{reqId}/reject")
    public ParticipationRequestDto rejectParticipationRequestInEventOfCurrentUser(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @PathVariable(name = "reqId") Long requestId) {

        log.info("UserEventController: Получен {} запрос с параметрами: userId = {} eventId = {} requestId = {}",
                "PATCH /{userId}/events/{eventId}/requests/{reqId}/reject", userId, eventId, requestId);

        return requestService.rejectParticipationRequestInEventOfCurrentUser(userId, eventId, requestId);
    }

    @PostMapping("/{userId}/events/{eventId}/chat")
    public ChatDto openChat(@PathVariable Long userId, @PathVariable Long eventId) {

        log.info("UserEventController: Получен {} запрос с параметрами: userId = {} eventId = {}",
                "POST /{userId}/events/{eventId}/chat", userId, eventId);

        return chatService.openChat(userId, eventId);
    }

    @DeleteMapping("/{userId}/events/{eventId}/chats/{chatId}")
    public void closeChat(@PathVariable Long userId, @PathVariable Long eventId,  @PathVariable Long chatId) {

        log.info("UserEventController: Получен {} запрос с параметрами: userId = {} eventId = {} chatId = {}",
                "DELETE /{userId}/events/{eventId}/chats/{chatId}", userId, eventId, chatId);

        chatService.closeChat(userId, eventId, chatId);
    }
}
