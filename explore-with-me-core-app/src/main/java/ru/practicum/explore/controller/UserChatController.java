package ru.practicum.explore.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.dto.MessageDto;
import ru.practicum.explore.dto.NewMessageDto;
import ru.practicum.explore.service.ChatService;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserChatController {

    private final ChatService service;

    @GetMapping("/{userId}/chats/{chatId}")
    public List<MessageDto> viewChat(@PathVariable Long userId, @PathVariable Long chatId,
                                     @RequestParam(name = "from", defaultValue = "0")  Integer from,
                                     @RequestParam(name = "size", defaultValue = "10")  Integer size) {
        log.info("UserChatController: Получен {} запрос с параметрами: userId = {} chatId = {} from = {} size = {}",
                "GET /{userId}/chats/{chatId}", userId, chatId, from, size);

        return service.viewChat(userId, chatId, from, size);
    }

    @PostMapping("/{userId}/chats/{chatId}/messages")
    public List<MessageDto> writeMessage(@PathVariable Long userId, @PathVariable Long chatId,
                                         @Validated @RequestBody NewMessageDto newMessageDto,
                                         @RequestParam(name = "from", defaultValue = "0")  Integer from,
                                         @RequestParam(name = "size", defaultValue = "10")  Integer size) {
        log.info("UserChatController: Получен {} запрос с параметрами: userId = {} chatId = {} newMessageDto = {} from = {} size = {}",
                "POST /users/{userId}/chats/{chatId}/messages", userId, chatId, newMessageDto, from, size);

        return service.writeMessage(userId, chatId, newMessageDto, from, size);
    }

    @PatchMapping("/{userId}/chats/{chatId}/messages/{messId}")
    public List<MessageDto> editMessage(@PathVariable Long userId, @PathVariable Long chatId, @PathVariable Long messId,
                                        @Validated @RequestBody NewMessageDto newMessageDto,
                                        @RequestParam(name = "from", defaultValue = "0")  Integer from,
                                        @RequestParam(name = "size", defaultValue = "10")  Integer size) {
        log.info("UserChatController: Получен {} запрос с параметрами: userId = {} chatId = {} messId = {} newMessageDto = {}  from = {} size = {}",
                "POST /users/{userId}/chats/{chatId}/messages", userId, chatId, messId, newMessageDto, from, size);

        return service.editMessage(userId, chatId, messId, newMessageDto, from, size);
    }

    @DeleteMapping("/{userId}/chats/{chatId}/messages/{messId}")
    public List<MessageDto> deleteMessage(@PathVariable Long userId, @PathVariable Long chatId, @PathVariable Long messId,
                                          @RequestParam(name = "from", defaultValue = "0")  Integer from,
                                          @RequestParam(name = "size", defaultValue = "10")  Integer size) {
        log.info("UserChatController: Получен {} запрос с параметрами: userId = {} chatId = {} messId = {} from = {} size = {}",
                "POST /users/{userId}/chats/{chatId}/messages", userId, chatId, messId, from, size);

        return service.deleteMessage(userId, chatId, messId, from, size);
    }
}
