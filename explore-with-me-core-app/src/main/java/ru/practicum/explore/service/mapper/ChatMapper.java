package ru.practicum.explore.service.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.explore.dto.AdminChatDto;
import ru.practicum.explore.dto.ChatDto;
import ru.practicum.explore.dto.MessageDto;
import ru.practicum.explore.model.Chat;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChatMapper {

    public static ChatDto toChatDto(Chat chat) {
        ChatDto chatDto = new ChatDto();
        chatDto.setId(chat.getId());
        chatDto.setEvent(EventMapper.toEventMiniDto(chat.getEvent()));
        chatDto.setOwner(UserMapper.toUserShortDto(chat.getInitiator()));
        chatDto.setCreationDate(chat.getCreationDate());
        chatDto.setStatus(chat.getStatus());
        return chatDto;
    }

    public static AdminChatDto toAdminChatDto(Chat chat, List<MessageDto> messagesDto) {
        AdminChatDto adminChatDto = new AdminChatDto();
        adminChatDto.setId(chat.getId());
        adminChatDto.setEvent(EventMapper.toEventMiniDto(chat.getEvent()));
        adminChatDto.setOwner(UserMapper.toUserShortDto(chat.getInitiator()));
        adminChatDto.setCreationDate(chat.getCreationDate());
        adminChatDto.setStatus(chat.getStatus());
        adminChatDto.setMessages(messagesDto);
        return adminChatDto;
    }
}
