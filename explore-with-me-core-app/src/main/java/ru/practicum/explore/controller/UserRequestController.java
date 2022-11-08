package ru.practicum.explore.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.dto.ChatDto;
import ru.practicum.explore.dto.ParticipationRequestDto;
import ru.practicum.explore.service.ChatService;
import ru.practicum.explore.service.ParticipationRequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
public class UserRequestController {

    private final ParticipationRequestService service;
    private final ChatService chatService;

    @GetMapping("/{userId}/requests")
    public List<ParticipationRequestDto> getUserRequests(@PathVariable Long userId) {
        log.info("UserRequestController: Получен {} запрос с параметрами: userId = {}", "GET /{userId}/requests", userId);
        return service.getUserRequests(userId);
    }

    @PostMapping("/{userId}/requests")
    public ParticipationRequestDto addParticipationRequestByUserForEvent(@PathVariable Long userId,
                                                                         @RequestParam Long eventId) {
        log.info("UserRequestController: Получен {} запрос с параметрами: userId = {} eventId = {}",
                "POST /{userId}/requests", userId, eventId);
        return service.addParticipationRequestByUserForEvent(userId, eventId);
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelParticipationRequestByUser(@PathVariable Long userId,
                                                                    @PathVariable Long requestId) {
        log.info("UserRequestController: Получен {} запрос с параметрами: userId = {} requestId = {}",
                "PATCH /{userId}/requests/{requestId}/cancel", userId, requestId);
        return service.cancelParticipationRequestByUser(userId,requestId);
    }

    @GetMapping("/{userId}/requests/{requestId}/chats")
    public ChatDto getChatForRequestByUser(@PathVariable Long userId, @PathVariable Long requestId) {

        log.info("UserRequestController: Получен {} запрос с параметрами: userId = {} requestId = {}",
                "GET /{userId}/requests/{requestId}/chats", userId, requestId);

        return chatService.getChatForRequestByUser(userId, requestId);
    }
}
