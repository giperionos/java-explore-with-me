package ru.practicum.explore.service;

import ru.practicum.explore.controller.AdminChatFilter;
import ru.practicum.explore.dto.*;

import java.util.List;

public interface ChatService {
    ChatDto openChat(Long userId, Long eventId);

    void closeChat(Long userId, Long eventId, Long chatId);

    ChatDto getChatForRequestByUser(Long userId, Long requestId);

    List<MessageDto> viewChat(Long userId, Long chatId, Integer from, Integer size);

    List<MessageDto> writeMessage(Long userId, Long chatId, NewMessageDto newMessageDto, Integer from, Integer size);

    List<MessageDto> editMessage(Long userId, Long chatId, Long messId, NewMessageDto newMessageDto, Integer from, Integer size);

    List<MessageDto> deleteMessage(Long userId, Long chatId, Long messId, Integer from, Integer size);

    List<ChatDto> getChatsByFilter(AdminChatFilter filter);

    AdminChatDto viewChatByAdmin(Long chatId, Integer from, Integer size);

    ChatDto updateChatByAdmin(Long chatId, AdminChatAction from);

    void deleteMessageByAdmin(Long chatId, Long messId);
}
