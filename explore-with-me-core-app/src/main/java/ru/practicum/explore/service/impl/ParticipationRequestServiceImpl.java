package ru.practicum.explore.service.impl;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.explore.dto.EventState;
import ru.practicum.explore.dto.ParticipationRequestDto;
import ru.practicum.explore.dto.RequestStatus;
import ru.practicum.explore.model.Event;
import ru.practicum.explore.model.ParticipationRequest;
import ru.practicum.explore.model.QParticipationRequest;
import ru.practicum.explore.model.User;
import ru.practicum.explore.repository.ParticipationRequestRepository;
import ru.practicum.explore.service.EventService;
import ru.practicum.explore.service.ParticipationRequestService;
import ru.practicum.explore.service.UserService;
import ru.practicum.explore.service.exceptions.*;
import ru.practicum.explore.service.mapper.RequestMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ParticipationRequestServiceImpl implements ParticipationRequestService {

    private final ParticipationRequestRepository repository;
    private final UserService userService;
    private final EventService eventService;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        //Получение информации о заявках текущего пользователя на участие в чужих событиях
        //сначала убедиться, что такой пользователь вообще есть
        userService.findUserByIdOrThrowException(userId);

        //поиск по условию
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

        //убедиться, что такой пользователь вообще есть
        User user = userService.findUserByIdOrThrowException(userId);

        //убедиться, чта такое событие есть
        Event event = eventService.findEventByIdOrThrowException(eventId);

        //Если пользователь пытается добавить запрос на участие в своем же событии
        if (user.getId().longValue() == event.getInitiator().getId().longValue()) {
            throw new ParticipationRequestUserOwnEventException(user, event);
        }

        //нельзя участвовать в неопубликованном событии
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ParticipationRequestOnNotPublishedEvent(user, event);
        }

        //если у события достигнут лимит запросов на участие - необходимо вернуть ошибку
        //Предварительно нужно посчитать Количество одобренных заявок на участие в данном событии
        Predicate byConfirmedStatus = QParticipationRequest.participationRequest.status.eq(RequestStatus.CONFIRMED);
        Predicate byEventId = QParticipationRequest.participationRequest.event.id.eq(eventId);
        Predicate eventIdAndConfirmedRequestPredicate = ExpressionUtils.allOf(byConfirmedStatus, byEventId);

        List<ParticipationRequest> alreadyExistConfirmedRequests = repository.findAll(eventIdAndConfirmedRequestPredicate, Pageable.unpaged())
                .stream().collect(Collectors.toList());

        if (event.getParticipantLimit() != null && event.getParticipantLimit() != 0 && event.getParticipantLimit() == alreadyExistConfirmedRequests.size()) {
            throw new ParticipationRequestLimitReachedException(user, event);
        }

        //нельзя добавить повторный запрос
        Predicate byRequesterId = QParticipationRequest.participationRequest.requester.id.eq(userId);
        Predicate byRequesterIdAndEventId = ExpressionUtils.allOf(byRequesterId, byEventId);
        List<ParticipationRequest> requestsForEventAndRequester = repository.findAll(byRequesterIdAndEventId, Pageable.unpaged())
                .stream().collect(Collectors.toList());

        if (!requestsForEventAndRequester.isEmpty()) {
            throw new ParticipationRequestAlreadyExistsException(user, event);
        }

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
        //Отмена своего запроса на участие в событии
        //убедиться, что такой пользователь вообще есть
        User user = userService.findUserByIdOrThrowException(userId);

        //убедиться, чта такой запрос есть для данного пользователя
        ParticipationRequest canceledRequest = findRequestByIdAndRequesterOrThrowException(requestId, user);

        //закрыть запрос
        canceledRequest.setStatus(RequestStatus.CANCELED);

        return RequestMapper.toParticipationRequestDto(repository.save(canceledRequest));
    }

    @Override
    public List<ParticipationRequestDto> getParticipationRequestsInEventOfCurrentUser(Long userId, Long eventId) {
        //Получение информации о запросах на участие в событии текущего пользователя

        //убедиться, что такой пользователь вообще есть
        User initiator = userService.findUserByIdOrThrowException(userId);

        //убедиться, чта такое событие есть
        Event event = eventService.findEventByIdOrThrowException(eventId);

        //убедиться, что данное событие принадлежит этому пользователю
        if (initiator.getId().longValue() != event.getInitiator().getId().longValue()) {
            throw new UserHaveNoAccessEventException(initiator, event);
        }

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

        //убедиться, что такой пользователь вообще есть
        User initiator = userService.findUserByIdOrThrowException(userId);

        //убедиться, чта такое событие есть
        Event event = eventService.findEventByIdOrThrowException(eventId);

        //убедиться, чта такой запрос есть
        ParticipationRequest request = findRequestByIdOrThrowException(requestId);

        //убедиться, что данное событие принадлежит этому пользователю
        if (initiator.getId().longValue() != event.getInitiator().getId().longValue()) {
            throw new UserHaveNoAccessEventException(initiator, event);
        }

        //если для события лимит заявок равен 0 или отключена пре-модерация заявок, то подтверждение заявок не требуется
        if (event.getParticipantLimit() != null && event.getParticipantLimit() == 0 || !event.isRequestModeration()) {
            return RequestMapper.toParticipationRequestDto(request);
        }

        //нельзя подтвердить заявку, если уже достигнут лимит по заявкам на данное событие
        Predicate byConfirmedStatus = QParticipationRequest.participationRequest.status.eq(RequestStatus.CONFIRMED);
        Predicate byEventId = QParticipationRequest.participationRequest.event.id.eq(eventId);
        Predicate eventIdAndConfirmedRequestPredicate = ExpressionUtils.allOf(byConfirmedStatus, byEventId);

        List<ParticipationRequest> alreadyExistConfirmedRequests = repository.findAll(eventIdAndConfirmedRequestPredicate, Pageable.unpaged())
                .stream().collect(Collectors.toList());

        if (event.getParticipantLimit() != null && event.getParticipantLimit() != 0 && event.getParticipantLimit() == alreadyExistConfirmedRequests.size()) {
            throw new ParticipationRequestLimitReachedException(initiator, event);
        }

        //если при подтверждении данной заявки, лимит заявок для события исчерпан, то все неподтверждённые заявки необходимо отклонить
        if (event.getParticipantLimit() != null && event.getParticipantLimit() != 0 && event.getParticipantLimit() == alreadyExistConfirmedRequests.size() + 1) {
            Predicate byPendingStatus = QParticipationRequest.participationRequest.status.eq(RequestStatus.PENDING);
            Predicate eventIdAndPendingRequestPredicate = ExpressionUtils.allOf(byPendingStatus, byEventId);

            //получить список все заявок, ожидающих подтверждения - отказать
            List<ParticipationRequest> pendingRequests = repository.findAll(eventIdAndPendingRequestPredicate, Pageable.unpaged())
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

        //убедиться, что такой пользователь вообще есть
        User initiator = userService.findUserByIdOrThrowException(userId);

        //убедиться, чта такое событие есть
        Event event = eventService.findEventByIdOrThrowException(eventId);

        //убедиться, чта такой запрос есть
        ParticipationRequest request = findRequestByIdOrThrowException(requestId);

        //убедиться, что данное событие принадлежит этому пользователю
        if (initiator.getId().longValue() != event.getInitiator().getId().longValue()) {
            throw new UserHaveNoAccessEventException(initiator, event);
        }

        //отказать заявку
        request.setStatus(RequestStatus.REJECTED);

        return RequestMapper.toParticipationRequestDto(repository.save(request));
    }

    @Override
    public ParticipationRequest findRequestByIdAndRequesterOrThrowException(Long requestId, User requester) {

        Predicate byUserId = QParticipationRequest.participationRequest.requester.id.eq(requester.getId());
        Predicate byRequestId = QParticipationRequest.participationRequest.id.eq(requestId);

        Predicate finalPredicate = ExpressionUtils.allOf(byRequestId, byUserId);
        List<ParticipationRequest> requests = repository.findAll(finalPredicate, Pageable.unpaged())
                .stream().collect(Collectors.toList());

        if (requests.isEmpty()) {
            throw new ParticipationRequestForRequesterNotFoundException(requester, requestId);
        }

        if (requests.size() > 1) {
            throw new UnexpectedException("findRequestByIdAndRequesterOrThrowException: Не предвиденная ошибка: не ожидалось больше одного результата!");
        }

         return requests.get(0);
    }

    @Override
    public ParticipationRequest findRequestByIdOrThrowException(Long requestId) {
        return repository.findById(requestId)
                .orElseThrow(() -> new ParticipationRequestNotFoundException(requestId));
    }
}
