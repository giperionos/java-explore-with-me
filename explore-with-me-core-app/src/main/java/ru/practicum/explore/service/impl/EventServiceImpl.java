package ru.practicum.explore.service.impl;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.EndpointViewParamsHelper;
import ru.practicum.explore.StatsClientService;
import ru.practicum.explore.config.Config;
import ru.practicum.explore.dto.*;
import ru.practicum.explore.model.*;
import ru.practicum.explore.repository.CategoryRepository;
import ru.practicum.explore.repository.EventRepository;
import ru.practicum.explore.repository.ParticipationRequestRepository;
import ru.practicum.explore.repository.UserRepository;
import ru.practicum.explore.service.EventService;
import ru.practicum.explore.service.exceptions.*;
import ru.practicum.explore.service.mapper.EventMapper;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private static final String APP_NAME = "ExploreWithMeApp";
    private static final String URI_PATTERN = "/event/%s";
    private final StatsClientService statsClientService;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ParticipationRequestRepository requestRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsByFilter(UserEventFilter filter, HttpServletRequest request) {

        /**
         * 1.сначала получить все события из БД согласно фильтру
         * 2. преобразовать event в eventShort
         * 3. отсортировать по нужному типу сортировки и отдать результат
         */

        //сначала получить все события из БД согласно фильтру
        List<Predicate> predicates = new ArrayList<>();

        //это публичный эндпоинт, соответственно в выдаче должны быть только опубликованные события
        predicates.add(QEvent.event.state.eq(EventState.PUBLISHED));

        //текстовый поиск (по аннотации и подробному описанию) должен быть без учета регистра букв
        if (!filter.getTextSearch().isBlank()) {
            predicates.add(
                    QEvent.event.annotation.containsIgnoreCase(filter.getTextSearch())
                            .or(QEvent.event.description.containsIgnoreCase(filter.getTextSearch()))
            );
        }

        //если в запросе не указан диапазон дат [rangeStart-rangeEnd], то нужно выгружать события, которые произойдут позже текущей даты и времени
        LocalDateTime start = filter.getRangeStartEncoded().isBlank() ?
                LocalDateTime.now() :
                LocalDateTime.parse(URLDecoder.decode(filter.getRangeStartEncoded(), StandardCharsets.UTF_8), Config.formatter);


        LocalDateTime end = filter.getRangeEndEncoded().isBlank() ?
                LocalDateTime.now().plusYears(1) :
                LocalDateTime.parse(URLDecoder.decode(filter.getRangeEndEncoded(), StandardCharsets.UTF_8), Config.formatter);

        predicates.add(QEvent.event.eventDate.between(start,end));

        //если пришли конкретные категории
        if (filter.getCategoriesIds().size() > 0) {
            predicates.add(QEvent.event.category.in(getCategoryByIds(filter.getCategoriesIds())));
        }

        //поиск только платных/бесплатных событий
        if (filter.getPaid() != null) {
            predicates.add(QEvent.event.paid.eq(filter.getPaid()));
        }

        //Получить предварительный список из БД
        Predicate finalPredicate = ExpressionUtils.allOf(predicates);
        List<Event> preResultsEventList = eventRepository.findAll(finalPredicate, Pageable.unpaged()).toList();

        //только события у которых не исчерпан лимит запросов на участие
        //Если такой признак пришел, предварительно сходить за заявками
        if (filter.getOnlyAvailableByRequestLimit() != null && filter.getOnlyAvailableByRequestLimit()) {
            preResultsEventList.stream().filter((event -> {
                //если у мероприятия нет ограничения не лимит запросов
                 if (event.getParticipantLimit() == 0) {
                     //значит оно должно попасть в результат
                     return true;
                 }

                 //значит нужно сходить за кол-во заявок
                 List<ParticipationRequest> alreadyExistsRequests = findAllRequestsByEventIdsAndStatus(List.of(event.getId()), RequestStatus.CONFIRMED);

                 //если найденное кол-во заявок меньше лимита для события
                 if (alreadyExistsRequests.size() < event.getParticipantLimit()) {
                     //значит оно должно попасть в результат
                     return true;
                 }

                 //если ничего выше не случилось, значит событие не подходит
                 return false;
            }));
        }

        //преобразовать полученный список Event в список EventShortDto
        List<EventShortDto> preResultsEventShortList = preResultsEventList
                .stream()
                .map(this::mapEventToEventShortDto)
                .collect(Collectors.toList());


        //теперь выбрать способ сортировки вывода результатов и сформировать результат с учетом пагинации
        List<EventShortDto> result;
        switch (filter.getSortType()) {
            case VIEWS:
                result = preResultsEventShortList
                        .stream()
                        .sorted(Comparator.comparing(EventShortDto::getViews))
                        .skip(filter.getFrom())
                        .limit(filter.getSize())
                        .collect(Collectors.toList());
                break;

            case EVENT_DATE:
                result = preResultsEventShortList
                        .stream()
                        .sorted(Comparator.comparing(EventShortDto::getEventDate))
                        .skip(filter.getFrom())
                        .limit(filter.getSize())
                        .collect(Collectors.toList());
                break;

            default:
                result = preResultsEventShortList
                        .stream()
                        .sorted(Comparator.comparing(EventShortDto::getId))
                        .skip(filter.getFrom())
                        .limit(filter.getSize())
                        .collect(Collectors.toList());
        }


        //информацию о том, что по этому эндпоинту был осуществлен и обработан запрос, нужно сохранить в сервисе статистики
        doHitRequest(request);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventWithFullInfoById(Long eventId, HttpServletRequest request) {
        //информация о событии должна включать в себя количество просмотров и количество подтвержденных запросов

        //найти событие по id + событие должно быть опубликовано
        List<Event> foundedEventList = findAllByIdsAndState(List.of(eventId), EventState.PUBLISHED);

        if (foundedEventList.isEmpty()) {
            throw new EventNotFoundException(eventId);
        }

        if (foundedEventList.size() > 1) {
            throw new UnexpectedException("getEventWithFullInfoById: Не предвиденная ошибка: не ожидалось больше одного результата!");
        }

        Event foundedEvent = foundedEventList.get(0);

        long confirmedRequests = getCountConfirmedRequestsForEvent(foundedEvent);
        long views = getCountViewsForEvent(foundedEvent);

        //Сформировать EventFullDto для ответа
        EventFullDto result = EventMapper.toEventFullDto(foundedEvent, confirmedRequests, views);

        //информацию о том, что по этому эндпоинту был осуществлен и обработан запрос, нужно сохранить в сервисе статистики
        doHitRequest(request);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsAddedByCurrentUser(Long userId, Integer from, Integer size) {
        //Получение событий, добавленных текущим пользователем
        //сначала убедиться, что такой пользователь вообще есть
        findUserByIdOrThrowException(userId);

        Predicate byUserId = QEvent.event.initiator.id.eq(userId);
        PageRequest pageRequest = PageRequest.of(from / size, size, Sort.by("id").ascending());

        List<Event> userEvents = eventRepository.findAll(byUserId, pageRequest).toList();

        return userEvents.stream().map(this::mapEventToEventShortDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEventAddedByCurrentUser(Long userId, UpdateEventRequestDto updateEventRequestDto) {
        //Изменение события добавленного текущим пользователем

        /**
         * изменить можно только отмененные события или события в состоянии ожидания модерации
         * если редактируется отменённое событие, то оно автоматически переходит в состояние ожидания модерации
         * дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента
         */

        //убедиться, что такой пользователь вообще есть
        User user = findUserByIdOrThrowException(userId);

        //убедиться, чта такое событие есть
        Event eventForUpdate = findEventByIdOrThrowException(updateEventRequestDto.getEventId());

        //убедиться, что пользователь редактируем своем мероприятие
        if (user.getId().longValue() != eventForUpdate.getInitiator().getId().longValue()) {
            throw new UserHaveNoAccessEventException(user, eventForUpdate);
        }

        //изменить можно только отмененные события или события в состоянии ожидания модерации
        if (eventForUpdate.getState().equals(EventState.PUBLISHED)) {
            throw new EventRestrictEditByStateException(eventForUpdate);
        }

        //обновить данные мероприятия: если они пришли и не совпадают с тем, что есть в текущем объекте
        //annotation
        if (!updateEventRequestDto.getAnnotation().isBlank()
                && !updateEventRequestDto.getAnnotation().equals(eventForUpdate.getAnnotation())) {
            eventForUpdate.setAnnotation(updateEventRequestDto.getAnnotation());
        }

        //category
        if (updateEventRequestDto.getCategory() != null
                && updateEventRequestDto.getCategory().longValue() != eventForUpdate.getCategory().getId().longValue()) {
            //проверить, что такая категория вообще есть
            Category newCategory = findCategoryByIdOrThrowException(updateEventRequestDto.getCategory());

            eventForUpdate.setCategory(newCategory);
        }

        //description
        if (!updateEventRequestDto.getDescription().isBlank()
                && !updateEventRequestDto.getDescription().equals(eventForUpdate.getDescription())) {
            eventForUpdate.setDescription(updateEventRequestDto.getDescription());
        }

        //eventDate
        if (updateEventRequestDto.getEventDate() != null) {
            //дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента
            if (updateEventRequestDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2L))) {
                throw new EventRestrictEditByDateException(eventForUpdate);
            }

            eventForUpdate.setEventDate(updateEventRequestDto.getEventDate());
        }

        //Paid
        if (updateEventRequestDto.getPaid() != null  && !updateEventRequestDto.getPaid().equals(eventForUpdate.isPaid())) {
            eventForUpdate.setPaid(updateEventRequestDto.getPaid());
        }

        //ParticipantLimit
        if (updateEventRequestDto.getParticipantLimit() != null
                && updateEventRequestDto.getParticipantLimit().intValue() != eventForUpdate.getParticipantLimit().intValue()) {
            eventForUpdate.setParticipantLimit(updateEventRequestDto.getParticipantLimit());
        }

        //Title
        if (!updateEventRequestDto.getTitle().isBlank() && !updateEventRequestDto.getTitle().equals(eventForUpdate.getTitle())) {
            eventForUpdate.setTitle(updateEventRequestDto.getTitle());
        }

        //сохранить в БД и вернуть
        long confirmedRequests = getCountConfirmedRequestsForEvent(eventForUpdate);
        long views = getCountViewsForEvent(eventForUpdate);
        return EventMapper.toEventFullDto(eventRepository.save(eventForUpdate), confirmedRequests, views);
    }

    @Override
    @Transactional
    public EventFullDto addNewEventByCurrentUser(Long userId, NewEventDto newEventDto) {
        //Добавление нового события

        //убедиться, что такой пользователь вообще есть
        User user = findUserByIdOrThrowException(userId);

        //получить категорию для этого мероприятия
        Category category = findCategoryByIdOrThrowException(newEventDto.getCategory());

        //дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2L))) {
            throw new EventRestrictAddByDateException(newEventDto);
        }

        Event newSavedEvent = eventRepository.save(EventMapper.toEvent(newEventDto, category, user));

        long confirmedRequests = getCountConfirmedRequestsForEvent(newSavedEvent);
        long views = getCountViewsForEvent(newSavedEvent);

        return EventMapper.toEventFullDto(newSavedEvent, confirmedRequests, views);
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getFullInfoEventAddedByCurrentUser(Long userId, Long eventId) {
        //Получение полной информации о событии добавленном текущим пользователем

        //убедиться, что такой пользователь вообще есть
        User user = findUserByIdOrThrowException(userId);

        //убедиться, чта такое событие есть
        Event event = findEventByIdOrThrowException(eventId);

        //убедиться, что пользователь получает инфо о своем мероприятие
        if (user.getId().longValue() != event.getInitiator().getId().longValue()) {
            throw new UserHaveNoAccessEventException(user, event);
        }

        long confirmedRequests = getCountConfirmedRequestsForEvent(event);
        long views = getCountViewsForEvent(event);

        return EventMapper.toEventFullDto(event, confirmedRequests, views);
    }

    @Override
    @Transactional
    public EventFullDto cancelEventAddedByCurrentUser(Long userId, Long eventId) {
        //Отмена события добавленного текущим пользователем.

        //убедиться, что такой пользователь вообще есть
        User user = findUserByIdOrThrowException(userId);

        //убедиться, чта такое событие есть
        Event event = findEventByIdOrThrowException(eventId);

        //убедиться, что пользователь получает инфо о своем мероприятие
        if (user.getId().longValue() != event.getInitiator().getId().longValue()) {
            throw new UserHaveNoAccessEventException(user, event);
        }

        //Отменить можно только событие в состоянии ожидания модерации.
        if (!event.getState().equals(EventState.PENDING)) {
            throw new EventRestrictEditByStateException(event);
        }

        event.setState(EventState.CANCELED);
        //сохранить в БД
        eventRepository.save(event);

        long confirmedRequests = getCountConfirmedRequestsForEvent(event);
        long views = getCountViewsForEvent(event);

        return EventMapper.toEventFullDto(event, confirmedRequests, views);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getEventsWithFullInfoByFilter(AdminEventFilter filter) {
        //возвращает полную информацию обо всех событиях подходящих под переданные условия

        //сначала получить все события из БД согласно фильтру
        List<Predicate> predicates = new ArrayList<>();

        //если пришел список пользователей
        if (filter.getUsersIds().size() > 0) {
            predicates.add(QEvent.event.initiator.id.in(filter.getUsersIds()));
        }

        //если пришел список состояний
        if (filter.getStates() != null && !filter.getStates().isEmpty()) {
            predicates.add(QEvent.event.state.in(filter.getStates()));
        }

        //если пришли конкретные категории
        if (filter.getCategoriesIds().size() > 0) {
            predicates.add(QEvent.event.category.in(getCategoryByIds(filter.getCategoriesIds())));
        }

        //если пришла дата начала для выборки
        if (!filter.getRangeStartEncoded().isBlank()) {
            LocalDateTime start = LocalDateTime.parse(URLDecoder.decode(filter.getRangeStartEncoded(), StandardCharsets.UTF_8), Config.formatter);

            LocalDateTime end;
            if (!filter.getRangeEndEncoded().isBlank()) {
                end = LocalDateTime.parse(URLDecoder.decode(filter.getRangeEndEncoded(), StandardCharsets.UTF_8), Config.formatter);
            } else {
                end = LocalDateTime.now().plusYears(1);
            }

            predicates.add(QEvent.event.eventDate.between(start,end));
        }

        int page = filter.getFrom() /  filter.getSize();
        PageRequest pageRequest = PageRequest.of(page, filter.getSize(), Sort.by("id").ascending());

        Predicate finalPredicate = ExpressionUtils.allOf(predicates);
        List<Event> events = eventRepository.findAll(finalPredicate, pageRequest).toList();



        return events.stream().map((event -> {
            long confirmedRequests = getCountConfirmedRequestsForEvent(event);
            long views = getCountViewsForEvent(event);

            return EventMapper.toEventFullDto(event, confirmedRequests, views);
        })).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto editEvent(Long eventId, AdminUpdateEventRequestDto adminUpdateEventRequestDto) {
        //Редактирование данных любого события администратором.

        //убедиться, чта такое событие есть
        Event eventForUpdate = findEventByIdOrThrowException(eventId);

        //обновить данные мероприятия: если они пришли и не совпадают с тем, что есть в текущем объекте
        //annotation
        if (!adminUpdateEventRequestDto.getAnnotation().isBlank()
                && !adminUpdateEventRequestDto.getAnnotation().equals(eventForUpdate.getAnnotation())) {
            eventForUpdate.setAnnotation(adminUpdateEventRequestDto.getAnnotation());
        }

        //category
        if (adminUpdateEventRequestDto.getCategory() != null
                && adminUpdateEventRequestDto.getCategory().longValue() != eventForUpdate.getCategory().getId().longValue()) {
            //проверить, что такая категория вообще есть
            Category newCategory = findCategoryByIdOrThrowException(adminUpdateEventRequestDto.getCategory());

            eventForUpdate.setCategory(newCategory);
        }

        //description
        if (!adminUpdateEventRequestDto.getDescription().isBlank()
                && !adminUpdateEventRequestDto.getDescription().equals(eventForUpdate.getDescription())) {
            eventForUpdate.setDescription(adminUpdateEventRequestDto.getDescription());
        }

        //eventDate
        if (adminUpdateEventRequestDto.getEventDate() != null
                && !adminUpdateEventRequestDto.getEventDate().equals(eventForUpdate.getEventDate())) {

            eventForUpdate.setEventDate(adminUpdateEventRequestDto.getEventDate());
        }

        //Location
        if (adminUpdateEventRequestDto.getLocation() != null) {
            eventForUpdate.setLat(adminUpdateEventRequestDto.getLocation().getLat());
            eventForUpdate.setLon(adminUpdateEventRequestDto.getLocation().getLon());
        }

        //Paid
        if (adminUpdateEventRequestDto.getPaid() != null  && !adminUpdateEventRequestDto.getPaid().equals(eventForUpdate.isPaid())) {
            eventForUpdate.setPaid(adminUpdateEventRequestDto.getPaid());
        }

        //ParticipantLimit
        if (adminUpdateEventRequestDto.getParticipantLimit() != null
                && adminUpdateEventRequestDto.getParticipantLimit().intValue() != eventForUpdate.getParticipantLimit().intValue()) {
            eventForUpdate.setParticipantLimit(adminUpdateEventRequestDto.getParticipantLimit());
        }

        //requestModeration
        if (adminUpdateEventRequestDto.getRequestModeration() != null
                && !adminUpdateEventRequestDto.getRequestModeration().equals(eventForUpdate.isRequestModeration())) {
            eventForUpdate.setRequestModeration(adminUpdateEventRequestDto.getRequestModeration());
        }

        //Title
        if (!adminUpdateEventRequestDto.getTitle().isBlank() && !adminUpdateEventRequestDto.getTitle().equals(eventForUpdate.getTitle())) {
            eventForUpdate.setTitle(adminUpdateEventRequestDto.getTitle());
        }

        //сохранить в БД и вернуть
        long confirmedRequests = getCountConfirmedRequestsForEvent(eventForUpdate);
        long views = getCountViewsForEvent(eventForUpdate);
        return EventMapper.toEventFullDto(eventRepository.save(eventForUpdate), confirmedRequests, views);
    }

    @Override
    @Transactional
    public EventFullDto publishEvent(Long eventId) {
        /**
         * дата начала события должна быть не ранее чем за час от даты публикации.
         * событие должно быть в состоянии ожидания публикации
         */

        //убедиться, чта такое событие есть
        Event eventForPublish = findEventByIdOrThrowException(eventId);

        LocalDateTime publishDateTime = LocalDateTime.now();

        if (eventForPublish.getEventDate().isBefore(publishDateTime.plusHours(1L))) {
            throw new EventRestrictPublishByDateException(eventForPublish);
        }

        if (!eventForPublish.getState().equals(EventState.PENDING)) {
            throw new EventRestrictPublishByStateException(eventForPublish);
        }

        eventForPublish.setState(EventState.PUBLISHED);
        eventForPublish.setPublishDate(publishDateTime);

        long confirmedRequests = getCountConfirmedRequestsForEvent(eventForPublish);
        long views = getCountViewsForEvent(eventForPublish);
        return EventMapper.toEventFullDto(eventRepository.save(eventForPublish), confirmedRequests, views);
    }

    @Override
    @Transactional
    public EventFullDto rejectEvent(Long eventId) {
        //Отклонение события

        //событие не должно быть опубликовано.
        Event eventForReject = findEventByIdOrThrowException(eventId);

        if (eventForReject.getState().equals(EventState.PUBLISHED)) {
            throw new EventRestrictRejectByStateException(eventForReject);
        }

        eventForReject.setState(EventState.CANCELED);

        long confirmedRequests = getCountConfirmedRequestsForEvent(eventForReject);
        long views = getCountViewsForEvent(eventForReject);
        return EventMapper.toEventFullDto(eventRepository.save(eventForReject), confirmedRequests, views);
    }

    @Override
    public EndpointHitDto doHitRequest(HttpServletRequest request) {
        EndpointHitDto endpointHitDto = new EndpointHitDto();
        endpointHitDto.setApp(APP_NAME);
        endpointHitDto.setUri(request.getRequestURI());
        endpointHitDto.setIp(request.getRemoteAddr());
        endpointHitDto.setTimestamp(LocalDateTime.now().format(Config.formatter));
        return statsClientService.hit(endpointHitDto);
    }

    private List<Event> findAllByIdsAndState(List<Long> eventIds, EventState state) {
        return  eventRepository.findAllById(eventIds)
                .stream()
                .filter((event) -> event.getState().equals(state))
                .collect(Collectors.toList());
    }

    private List<ParticipationRequest> findAllRequestsByEventIdsAndStatus(List<Long> eventIds, RequestStatus status) {
        return requestRepository.findAllById(eventIds)
                .stream()
                .filter((participationRequest -> participationRequest.getStatus().equals(status)))
                .collect(Collectors.toList());
    }

    @Override
    public EventShortDto mapEventToEventShortDto(Event event) {

        long confirmedRequests = getCountConfirmedRequestsForEvent(event);
        long views = getCountViewsForEvent(event);

        return EventMapper.toEventShortDto(event, confirmedRequests, views);
    }

    private long getCountConfirmedRequestsForEvent(Event event) {
        //информация о каждом событии должна включать в себя количество просмотров и количество уже одобренных заявок на участие
        List<ParticipationRequest> confirmedRequestsList = findAllRequestsByEventIdsAndStatus(List.of(event.getId()), RequestStatus.CONFIRMED);
        return confirmedRequestsList.size();
    }

    private long getCountViewsForEvent(Event event) {
        //нужно получить статистку просмотров по событию
        //сформировать фильтр
        EndpointViewParamsHelper viewFilter = EndpointViewParamsHelper.ofOriginalValues(
                LocalDateTime.now().minusYears(1),
                LocalDateTime.now(),
                List.of(String.format(URI_PATTERN, event.getId())),
                Boolean.FALSE);

        //сделать запрос
        List<EndpointViewDto> eventStatistics = statsClientService.getEndpointStatsByParams(viewFilter.getQuery());

        return eventStatistics.size();
    }

    private List<Category> getCategoryByIds(List<Long> ids) {
        return categoryRepository.findAllById(ids);
    }

    private Category findCategoryByIdOrThrowException(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new CategoryNotFoundException(catId));
    }

    private User findUserByIdOrThrowException(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private Event findEventByIdOrThrowException(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
    }
}
