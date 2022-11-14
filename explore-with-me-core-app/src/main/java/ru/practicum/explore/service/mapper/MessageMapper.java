package ru.practicum.explore.service.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.explore.dto.MessageDto;
import ru.practicum.explore.dto.MessageStatus;
import ru.practicum.explore.dto.NewMessageDto;
import ru.practicum.explore.model.Chat;
import ru.practicum.explore.model.Message;
import ru.practicum.explore.model.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageMapper {
    public static MessageDto toMessageDto(Message message) {
        MessageDto messageDto = new MessageDto();
        messageDto.setId(message.getId());
        messageDto.setSendDate(message.getCreationDate());
        messageDto.setAuthorName(message.getAuthor().getName());
        messageDto.setText(message.getText());
        return messageDto;
    }

    public static Message toMessage(NewMessageDto newMessageDto, Chat chat, User user, MessageStatus status) {
         Message message = new Message();
         message.setText(newMessageDto.getText());
         message.setChat(chat);
         message.setAuthor(user);
         message.setStatus(status);
         return message;
    }
}
