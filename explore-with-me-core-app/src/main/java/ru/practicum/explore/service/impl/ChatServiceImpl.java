package ru.practicum.explore.service.impl;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.config.Config;
import ru.practicum.explore.controller.AdminChatFilter;
import ru.practicum.explore.dto.*;
import ru.practicum.explore.model.*;
import ru.practicum.explore.repository.ChatRepository;
import ru.practicum.explore.repository.EventRepository;
import ru.practicum.explore.repository.MessageRepository;
import ru.practicum.explore.repository.ParticipationRequestRepository;
import ru.practicum.explore.service.ChatService;
import ru.practicum.explore.service.exceptions.*;
import ru.practicum.explore.service.mapper.ChatMapper;
import ru.practicum.explore.service.mapper.MessageMapper;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Transactional
@Service
@Slf4j
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final EventRepository eventRepository;
    private final ParticipationRequestRepository requestRepository;

    @Override
    public ChatDto openChat(Long userId, Long eventId) {
        //Нельзя повторно создать чат для инициатора userId, для eventId и статуса OPENED
        BooleanExpression byInitiatorIdInChats = QChat.chat.initiator.id.eq(userId);
        BooleanExpression byEventIdIdInChats = QChat.chat.event.id.eq(eventId);
        BooleanExpression byStatusInChats = QChat.chat.status.eq(ChatStatus.OPENED);

        Chat alreadyExistChat = chatRepository.findOne(byInitiatorIdInChats.and(byEventIdIdInChats).and(byStatusInChats))
                .orElse(null);

        //если такой чат уже есть - вернуть его
        if (alreadyExistChat != null) {
            return ChatMapper.toChatDto(alreadyExistChat);
        }

        //создать чат для eventId, в котором инициатор userId
        //создать чат можно только для опубликованного события
        BooleanExpression byEventId = QEvent.event.id.eq(eventId);
        BooleanExpression byInitiatorId = QEvent.event.initiator.id.eq(userId);
        BooleanExpression byState = QEvent.event.state.eq(EventState.PUBLISHED);

        Event event = eventRepository.findOne(byEventId.and(byInitiatorId).and(byState))
                .orElseThrow(() -> new EventNotFoundException(eventId));

        Chat chat = new Chat();
        chat.setInitiator(event.getInitiator());
        chat.setEvent(event);
        chat.setStatus(ChatStatus.OPENED);

        return ChatMapper.toChatDto(chatRepository.save(chat));
    }

    @Override
    public void closeChat(Long userId, Long eventId, Long chatId) {

        BooleanExpression byChatId = QChat.chat.id.eq(chatId);
        BooleanExpression byInitiatorId = QChat.chat.initiator.id.eq(userId);
        BooleanExpression byEventId = QChat.chat.event.id.eq(eventId);
        BooleanExpression byState = QChat.chat.status.notIn(ChatStatus.CLOSED);

        Chat chat = chatRepository.findOne(byChatId.and(byInitiatorId).and(byEventId).and(byState))
                .orElseThrow(() -> new ChatNotFoundException(chatId));

        chat.setStatus(ChatStatus.CLOSED);
        chatRepository.save(chat);
    }

    @Override
    @Transactional(readOnly = true)
    public ChatDto getChatForRequestByUser(Long userId, Long requestId) {
        //участник мероприятия в рамках своего запроса на участия, хочет получить чат
        //только по одобренной заявке на участие можно получить чат,
        //чат должен быть открыт

        BooleanExpression byRequestId = QParticipationRequest.participationRequest.id.eq(requestId);
        BooleanExpression byUserId = QParticipationRequest.participationRequest.requester.id.eq(userId);
        BooleanExpression byStatus = QParticipationRequest.participationRequest.status.eq(RequestStatus.CONFIRMED);

        ParticipationRequest request = requestRepository.findOne(byRequestId.and(byUserId).and(byStatus))
                .orElseThrow(() -> new ParticipationRequestForRequesterNotFoundException(userId, requestId));

        //из полученного одобренного запроса на участие нужно взять событие и по нему найти отрытый чат
        BooleanExpression byEventId = QChat.chat.event.id.eq(request.getEvent().getId());
        BooleanExpression byState = QChat.chat.status.eq(ChatStatus.OPENED);

        Chat chat = chatRepository.findOne(byEventId.and(byState))
                .orElseThrow(() -> new ChatForRequestNotFoundException(requestId));

        return ChatMapper.toChatDto(chatRepository.save(chat));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageDto> viewChat(Long userId, Long chatId, Integer from, Integer size) {
        //Проверка, что данный чат ест и открыт,
        Chat chat = getChatOrThrowException(chatId, ChatStatus.OPENED);

        //Проверить, что данный пользователь может его читать и писать
        checkAccessUserForChat(userId, chatId);

        //получить список отсортированных сообщений чата
        return getMessagesForChat(chat, from, size);
    }

    @Override
    public List<MessageDto> writeMessage(Long userId, Long chatId, NewMessageDto newMessageDto, Integer from, Integer size) {
        //Проверка, что данный чат ест и открыт,
        Chat chat = getChatOrThrowException(chatId, ChatStatus.OPENED);

        //Проверить, что данный пользователь может его читать и писать
        User user = checkAccessUserForChat(userId, chatId);

        //добавить сообщение в чат
        Message message = MessageMapper.toMessage(newMessageDto, chat, user, MessageStatus.PUBLISHED);
        messageRepository.save(message);

        //получить список отсортированных сообщений чата
        return getMessagesForChat(chat, from, size);
    }

    @Override
    public List<MessageDto> editMessage(Long userId, Long chatId, Long messId, NewMessageDto newMessageDto, Integer from, Integer size) {
        //получить сообщение
        Message messageForUpdate = findMessageOrThrowException(userId, chatId, messId, MessageStatus.PUBLISHED);

        messageForUpdate.setText(newMessageDto.getText());
        messageRepository.save(messageForUpdate);

        //получить список отсортированных сообщений чата
        return getMessagesForChat(messageForUpdate.getChat(), from, size);
    }

    @Override
    public List<MessageDto> deleteMessage(Long userId, Long chatId, Long messId, Integer from, Integer size) {
        //получить сообщение
        Message messageForDelete = findMessageOrThrowException(userId, chatId, messId, MessageStatus.PUBLISHED);

        messageForDelete.setStatus(MessageStatus.DELETED);
        messageRepository.save(messageForDelete);

        //получить список отсортированных сообщений чата
        return getMessagesForChat(messageForDelete.getChat(), from, size);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatDto> getChatsByFilter(AdminChatFilter filter) {

        //в фильтре параметров вообще может не быть,
        //поэтому набрать только то, что пришло
        List<Predicate> predicates = new ArrayList<>();

        if (filter.getEventIds() != null && !filter.getEventIds().isEmpty()) {
            predicates.add(
                    QChat.chat.event.id.in(filter.getEventIds())
            );
        }

        if (filter.getStatus() != null) {
            predicates.add(QChat.chat.status.eq(filter.getStatus()));
        }

        //даты учитывать, только если пришли
        if (filter.getRangeStartEncoded() != null && !filter.getRangeStartEncoded().isBlank()
                && filter.getRangeEndEncoded() != null && !filter.getRangeEndEncoded().isBlank()) {
            LocalDateTime start = LocalDateTime.parse(URLDecoder.decode(filter.getRangeStartEncoded(), StandardCharsets.UTF_8), Config.formatter);
            LocalDateTime end = LocalDateTime.parse(URLDecoder.decode(filter.getRangeEndEncoded(), StandardCharsets.UTF_8), Config.formatter);
            predicates.add(QChat.chat.creationDate.between(start, end));
        }

        //если условий поиска нет, значит получить все записи
        if (predicates.isEmpty()) {
            return chatRepository.findAll()
                    .stream()
                    .map(ChatMapper::toChatDto)
                    .sorted(Comparator.comparing(ChatDto::getCreationDate))
                    .skip(filter.getFrom())
                    .limit(filter.getSize())
                    .collect(Collectors.toList());
        }

        //если условия есть, то поиск с учетом условий
        Predicate finalPredicate = ExpressionUtils.allOf(predicates);

        return chatRepository.findAll(finalPredicate, Pageable.unpaged())
                .stream()
                .map(ChatMapper::toChatDto)
                .sorted(Comparator.comparing(ChatDto::getCreationDate))
                .skip(filter.getFrom())
                .limit(filter.getSize())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AdminChatDto viewChatByAdmin(Long chatId, Integer from, Integer size) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ChatNotFoundException(chatId));

        return ChatMapper.toAdminChatDto(chat, getMessagesForChat(chat, from, size));
    }

    @Override
    public ChatDto updateChatByAdmin(Long chatId, AdminChatAction action) {

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ChatNotFoundException(chatId));

        ChatStatus newStatus = action.equals(AdminChatAction.OPEN) ? ChatStatus.OPENED : ChatStatus.CLOSED;
        chat.setStatus(newStatus);

        return ChatMapper.toChatDto(chatRepository.save(chat));
    }

    @Override
    public void deleteMessageByAdmin(Long chatId, Long messId) {
        BooleanExpression byChatId = QMessage.message.chat.id.eq(chatId);
        BooleanExpression byMessId = QMessage.message.id.eq(messId);

        Message messageForDelete = messageRepository.findOne(byMessId.and(byChatId))
                .orElseThrow(() -> new MessageNotFoundException(messId));

        messageForDelete.setStatus(MessageStatus.DELETED);
        messageRepository.save(messageForDelete);
    }

    /**
     * проверить, что данный пользователь может читать и писать данный чат
     * @param userId - пользователь, для которого нужно сделать проверку
     * @param chatId - чат, для которого нужно сделать проверку
     * @return User - пользователь, который прошел проверку
     * @throws ChatAccessException
     */
    private User checkAccessUserForChat(Long userId, Long chatId) {
        //user либо инициатор, либо участник

        //получить чат
        Chat chat = getChatOrThrowException(chatId, ChatStatus.OPENED);

        //проверка на то, что user является инициатором
        if (chat.getInitiator().getId().equals(userId)) {
            return chat.getInitiator();
        }

        //проверка на то, что user является участником
        BooleanExpression byEventId = QParticipationRequest.participationRequest.event.id.eq(chat.getEvent().getId());
        BooleanExpression byStatus = QParticipationRequest.participationRequest.status.eq(RequestStatus.CONFIRMED);

        //Найти запрос на участие для мероприятия, где автора запроса - это текущий user с пришедшим userId
        List<ParticipationRequest> requests = requestRepository.findAll(byEventId.and(byStatus), Pageable.unpaged())
                .stream()
                .filter(request -> request.getRequester().getId().equals(userId))
                .collect(Collectors.toList());

        //если список содержит ровно один элемент, значит пользователь найден
        if (requests.size() == 1) {
            return requests.get(0).getRequester();
        } else {
            throw new ChatAccessException(userId, chatId);
        }
    }

    /**
     * @param chatId - id чата
     * @param status - статус чата
     * @return Chat - найденный чат
     * @throws ChatNotFoundException - если чат не найден
     */
    private Chat getChatOrThrowException(Long chatId, ChatStatus status) {

        BooleanExpression byChatId = QChat.chat.id.eq(chatId);
        BooleanExpression byState = QChat.chat.status.eq(status);

        return chatRepository.findOne(byChatId.and(byState))
                .orElseThrow(() -> new ChatNotFoundException(chatId));
    }

    private List<MessageDto> getMessagesForChat(Chat chat, Integer from, Integer size) {
        BooleanExpression byChatIdInMessages = QMessage.message.chat.id.eq(chat.getId());
        BooleanExpression byStatusInMessages = QMessage.message.status.in(MessageStatus.PUBLISHED);

        return messageRepository.findAll(byChatIdInMessages.and(byStatusInMessages), Pageable.unpaged())
                .stream()
                .map(MessageMapper::toMessageDto)
                .sorted(Comparator.comparing(MessageDto::getSendDate))
                .skip(from)
                .limit(size)
                .collect(Collectors.toList());
    }

    /**
     * @param userId - пользователь, автор сообщения
     * @param chatId - id чата
     * @param messId - id сообщения
     * @param status - статус сообщения
     * @return Message - найденное сообщение
     * @throws MessageNotFoundException - если сообщение не найдено
     */
    private Message findMessageOrThrowException(Long userId, Long chatId, Long messId, MessageStatus status) {
        BooleanExpression byMessageId = QMessage.message.id.eq(messId);
        BooleanExpression byChatId = QMessage.message.chat.id.eq(chatId);
        BooleanExpression byAuthorId = QMessage.message.author.id.eq(userId);
        BooleanExpression byStatus = QMessage.message.status.eq(status);

        return messageRepository.findOne(byMessageId.and(byChatId).and(byAuthorId).and(byStatus))
                .orElseThrow(() -> new MessageNotFoundException(messId));
    }
}
