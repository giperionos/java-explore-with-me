package ru.practicum.explore.service.impl;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.dto.EventState;
import ru.practicum.explore.dto.ParticipationRequestDto;
import ru.practicum.explore.dto.RequestStatus;
import ru.practicum.explore.model.*;
import ru.practicum.explore.repository.EventRepository;
import ru.practicum.explore.repository.ParticipationRequestRepository;
import ru.practicum.explore.repository.UserRepository;
import ru.practicum.explore.service.ParticipationRequestService;
import ru.practicum.explore.service.exceptions.*;
import ru.practicum.explore.service.mapper.RequestMapper;

import java.util.List;
import java.util.stream.Collectors;

@Transactional
@Service
@Slf4j
@RequiredArgsConstructor
public class ParticipationRequestServiceImpl implements ParticipationRequestService {

    private final ParticipationRequestRepository repository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        //Получение информации о заявках текущего пользователя на участие в чужих событиях
        Predicate byRequesterId = QParticipationRequest.participationRequest.requester.id.eq(userId);
        return repository.findAll(byRequesterId, Pageable.unpaged())
                .stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto addParticipationRequestByUserForEvent(Long userId, Long eventId) {
        /**
         * нельзя добавить повторный запрос
         * инициатор события не может добавить запрос на участие в своём событии
         * нельзя участвовать в неопубликованном событии
         * если у события достигнут лимит запросов на участие - необходимо вернуть ошибку
         * если для события отключена пре-модерация запросов на участие, то запрос должен автоматически перейти в состояние подтвержденного
         */
        //нельзя добавить повторный запрос
        BooleanExpression byRequesterIdInRequest = QParticipationRequest.participationRequest.requester.id.eq(userId);
        BooleanExpression byEventIdInRequest = QParticipationRequest.participationRequest.event.id.eq(eventId);
        repository.findOne(byRequesterIdInRequest.and(byEventIdInRequest))
                .ifPresent(request -> {
                    throw new ParticipationRequestAlreadyExistsException(userId, eventId);
                });

        //нельзя участвовать в неопубликованном событии
        BooleanExpression byEventIdInEvent = QEvent.event.id.eq(eventId);
        BooleanExpression byStateInEvent = QEvent.event.state.eq(EventState.PUBLISHED);

        //Найти событие
        Event event = eventRepository.findOne(byEventIdInEvent.and(byStateInEvent))
                .orElseThrow(() -> new EventNotFoundException(eventId));

        //Если пользователь пытается добавить запрос на участие в своем же событии
        if (userId == event.getInitiator().getId().longValue()) {
            throw new ParticipationRequestUserOwnEventException(userId, event);
        }

        //если у события достигнут лимит запросов на участие - необходимо вернуть ошибку
        //Предварительно нужно посчитать Количество одобренных заявок на участие в данном событии
        Predicate byConfirmedStatusInRequest = QParticipationRequest.participationRequest.status.eq(RequestStatus.CONFIRMED);

        List<ParticipationRequest> alreadyExistConfirmedRequests = repository.findAll(byRequesterIdInRequest.and(byConfirmedStatusInRequest), Pageable.unpaged())
                .stream().collect(Collectors.toList());

        if (event.getParticipantLimit() != null && event.getParticipantLimit() != 0 && event.getParticipantLimit() == alreadyExistConfirmedRequests.size()) {
            throw new ParticipationRequestLimitReachedException(event);
        }

        //найти текущего пользователя
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        //Создать заявку
        ParticipationRequest newRequest = new ParticipationRequest();
        newRequest.setRequester(user);
        newRequest.setEvent(event);
        //если для события отключена пре-модерация запросов на участие,
        //то запрос должен автоматически перейти в состояние подтвержденного
        RequestStatus newStatus = event.isRequestModeration() ? RequestStatus.PENDING : RequestStatus.CONFIRMED;
        newRequest.setStatus(newStatus);

        //сохранить
        return RequestMapper.toParticipationRequestDto(repository.save(newRequest));
    }

    @Override
    public ParticipationRequestDto cancelParticipationRequestByUser(Long userId, Long requestId) {
        BooleanExpression byRequestId = QParticipationRequest.participationRequest.id.eq(requestId);
        BooleanExpression byUserId = QParticipationRequest.participationRequest.requester.id.eq(userId);

        ParticipationRequest canceledRequest = repository.findOne(byRequestId.and(byUserId))
                .orElseThrow(() -> new ParticipationRequestForRequesterNotFoundException(userId, requestId));

        //закрыть запрос
        canceledRequest.setStatus(RequestStatus.CANCELED);

        return RequestMapper.toParticipationRequestDto(repository.save(canceledRequest));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getParticipationRequestsInEventOfCurrentUser(Long userId, Long eventId) {
        //Получение информации о запросах на участие в событии текущего пользователя
        //найти событие и убедиться, что текущий пользователя является его инициатором
        BooleanExpression byEventIdInEvent = QEvent.event.id.eq(eventId);
        BooleanExpression byInitiatorIdInEvent = QEvent.event.initiator.id.eq(userId);

        eventRepository.findOne(byEventIdInEvent.and(byInitiatorIdInEvent))
                .orElseThrow(() -> new EventNotFoundException(eventId));

        Predicate byEventId = QParticipationRequest.participationRequest.event.id.eq(eventId);

        return repository.findAll(byEventId, Pageable.unpaged())
                .stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto confirmParticipationRequestInEventOfCurrentUser(Long userId, Long eventId, Long requestId) {
        /**
         * если для события лимит заявок равен 0 или отключена пре-модерация заявок, то подтверждение заявок не требуется
         * нельзя подтвердить заявку, если уже достигнут лимит по заявкам на данное событие
         * если при подтверждении данной заявки, лимит заявок для события исчерпан, то все неподтверждённые заявки необходимо отклонить
         */

        //Сначала найти запрос с таким requestId, для такого eventId
        BooleanExpression byRequestIdInRequests = QParticipationRequest.participationRequest.id.eq(requestId);
        BooleanExpression byEventIdInRequests = QParticipationRequest.participationRequest.event.id.eq(eventId);

        ParticipationRequest request = repository.findOne(byRequestIdInRequests.and(byEventIdInRequests))
                .orElseThrow(() -> new ParticipationRequestNotFoundException(requestId));

        BooleanExpression byEventIdInEvent = QEvent.event.id.eq(eventId);
        BooleanExpression byInitiatorIdInEvent = QEvent.event.initiator.id.eq(userId);

        //Найти событие этого пользователя
        Event event = eventRepository.findOne(byEventIdInEvent.and(byInitiatorIdInEvent))
                .orElseThrow(() -> new EventNotFoundException(eventId));

        //если для события лимит заявок равен 0 или отключена пре-модерация заявок, то подтверждение заявок не требуется
        if (event.getParticipantLimit() != null && event.getParticipantLimit() == 0 || !event.isRequestModeration()) {
            return RequestMapper.toParticipationRequestDto(request);
        }

        //нельзя подтвердить заявку, если уже достигнут лимит по заявкам на данное событие
        BooleanExpression byConfirmedStatus = QParticipationRequest.participationRequest.status.eq(RequestStatus.CONFIRMED);
        BooleanExpression byEventId = QParticipationRequest.participationRequest.event.id.eq(eventId);

        List<ParticipationRequest> alreadyExistConfirmedRequests = repository.findAll(byEventId.and(byConfirmedStatus), Pageable.unpaged())
                .stream().collect(Collectors.toList());

        if (event.getParticipantLimit() != null && event.getParticipantLimit() != 0 && event.getParticipantLimit() == alreadyExistConfirmedRequests.size()) {
            throw new ParticipationRequestLimitReachedException(event);
        }

        //если при подтверждении данной заявки, лимит заявок для события исчерпан, то все неподтверждённые заявки необходимо отклонить
        if (event.getParticipantLimit() != null && event.getParticipantLimit() != 0 && event.getParticipantLimit() == alreadyExistConfirmedRequests.size() + 1) {
            BooleanExpression byPendingStatus = QParticipationRequest.participationRequest.status.eq(RequestStatus.PENDING);

            //получить список все заявок, ожидающих подтверждения - отказать
            List<ParticipationRequest> pendingRequests = repository.findAll(byEventId.and(byPendingStatus), Pageable.unpaged())
                    .stream()
                    .collect(Collectors.toList());

            //все неподтверждённые заявки необходимо отклонить
            pendingRequests.forEach((updatedRequest) -> updatedRequest.setStatus(RequestStatus.REJECTED));

            repository.saveAll(pendingRequests);
        }

        //подтвердить заявку
        request.setStatus(RequestStatus.CONFIRMED);

        return RequestMapper.toParticipationRequestDto(repository.save(request));
    }

    @Override
    public ParticipationRequestDto rejectParticipationRequestInEventOfCurrentUser(Long userId, Long eventId, Long requestId) {
        //Сначала найти запрос с таким requestId, для такого eventId
        BooleanExpression byRequestIdInRequests = QParticipationRequest.participationRequest.id.eq(requestId);
        BooleanExpression byEventIdInRequests = QParticipationRequest.participationRequest.event.id.eq(eventId);

        ParticipationRequest request = repository.findOne(byRequestIdInRequests.and(byEventIdInRequests))
                .orElseThrow(() -> new ParticipationRequestNotFoundException(requestId));

        BooleanExpression byEventIdInEvent = QEvent.event.id.eq(eventId);
        BooleanExpression byInitiatorIdInEvent = QEvent.event.initiator.id.eq(userId);

        //Проверить, что текущий  user является инициатором этого события
        eventRepository.findOne(byEventIdInEvent.and(byInitiatorIdInEvent))
                .orElseThrow(() -> new EventNotFoundException(eventId));

        //отказать заявку
        request.setStatus(RequestStatus.REJECTED);

        return RequestMapper.toParticipationRequestDto(repository.save(request));
    }
}
