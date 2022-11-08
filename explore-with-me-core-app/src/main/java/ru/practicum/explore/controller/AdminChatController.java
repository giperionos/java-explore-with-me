package ru.practicum.explore.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.dto.*;
import ru.practicum.explore.service.ChatService;

import java.util.List;

@RestController
@RequestMapping(path = "/admin/chats")
@RequiredArgsConstructor
@Slf4j
public class AdminChatController {

    private final ChatService service;

    @GetMapping
    public List<ChatDto> getChats(
            @RequestParam(name = "events", required = false) List<Long> eventIds,
            @RequestParam(name = "rangeStart", required = false) String rangeStartEncoded,
            @RequestParam(name = "rangeEnd", required = false) String rangeEndEncoded,
            @RequestParam(name = "status", required = false) ChatStatus status,
            @RequestParam(name = "from", defaultValue = "0")  Integer from,
            @RequestParam(name = "size", defaultValue = "10")  Integer size) {

        log.info("AdminChatController: Получен GET запрос с параметрами: "
                        + "events = {}"
                        + "rangeStart = {}"
                        + "rangeEnd = {}"
                        + "status = {}"
                        + "from = {}"
                        + "size = {}",
                eventIds,
                rangeStartEncoded,
                rangeEndEncoded,
                status,
                from,
                size);

        AdminChatFilter filter = AdminChatFilter.builder()
                .eventIds(eventIds)
                .rangeStartEncoded(rangeStartEncoded)
                .rangeEndEncoded(rangeEndEncoded)
                .status(status)
                .from(from)
                .size(size)
                .build();

        return service.getChatsByFilter(filter);
    }

    @GetMapping("/{chatId}")
    public AdminChatDto viewChatByAdmin(@PathVariable Long chatId,
                                        @RequestParam(name = "from", defaultValue = "0")  Integer from,
                                        @RequestParam(name = "size", defaultValue = "10")  Integer size) {

        log.info("AdminChatController: Получен {} запрос с параметрами: chatId = {} from = {} size = {}",
                "GET /admin/chats/{chatId}", chatId, from, size);

        return service.viewChatByAdmin(chatId, from, size);
    }

    @PatchMapping("/{chatId}")
    public ChatDto updateChatByAdmin(@PathVariable Long chatId,
                                     @RequestParam(name = "action") String actionStr) {

        log.info("AdminChatController: Получен {} запрос с параметрами: chatId = {} actionStr = {}",
                "PATCH /admin/chats/{chatId}?action=value", chatId, actionStr);

        return service.updateChatByAdmin(chatId, AdminChatAction.from(actionStr));
    }

    @DeleteMapping("/{chatId}/messages/{messId}")
    public void deleteMessageByAdmin(@PathVariable Long chatId, @PathVariable Long messId) {

        log.info("AdminChatController: Получен {} запрос с параметрами: chatId = {} messId = {}",
                "DELETE /admin/chats/{chatId}/messages/{messId}", chatId, messId);

        service.deleteMessageByAdmin(chatId, messId);
    }
}
