package ru.practicum.explore.service.impl;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.dto.CompilationDto;
import ru.practicum.explore.dto.EventShortDto;
import ru.practicum.explore.dto.NewCompilationDto;
import ru.practicum.explore.model.*;
import ru.practicum.explore.repository.CompilationEventRepository;
import ru.practicum.explore.repository.CompilationRepository;
import ru.practicum.explore.repository.EventRepository;
import ru.practicum.explore.service.CompilationService;
import ru.practicum.explore.service.EventService;
import ru.practicum.explore.service.exceptions.CompilationAlreadyExistsEventException;
import ru.practicum.explore.service.exceptions.CompilationNotContainEventForDeleteException;
import ru.practicum.explore.service.exceptions.CompilationNotFoundException;
import ru.practicum.explore.service.exceptions.EventNotFoundException;
import ru.practicum.explore.service.mapper.CompilationMapper;
import ru.practicum.explore.service.mapper.EventMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Transactional
@Service
@Slf4j
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationEventRepository compilationEventRepository;
    private final EventService eventService;

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {

        List<Compilation> compilations = new ArrayList<>();
        if (pinned != null && pinned) {
            Predicate byPinned = QCompilation.compilation.pinned.eq(pinned);
            compilations.addAll(compilationRepository.findAll(byPinned, Pageable.unpaged()).toList());
        } else {
            compilations.addAll(compilationRepository.findAll());
        }

        //сформировать список вообще всех событий из всех полученных подборок разом!
        //сначала получить все id подборок
        List<Long> compilationsIds = compilations
                .stream()
                .map((Compilation::getId))
                .collect(Collectors.toList());

        //по всем id подборок получить все id событий
        BooleanExpression byCompIds = QCompilationEvent.compilationEvent.compilationId.in(compilationsIds);
        List<Long> eventIds = compilationEventRepository.findAll(byCompIds, Pageable.unpaged())
                .stream()
                .map(CompilationEvent::getEventId)
                .collect(Collectors.toList());

        //получить все события по всем id
        List<Event> allEvents = eventRepository.findAllById(eventIds);

        //для всех этих событий разом получить заявки и просмотры и сформировать EventShortDto
        List<EventShortDto> allEventsShortDto = eventService.getFullDtoForAllEvents(allEvents)
                .stream()
                .map(EventMapper::toEventShortDtoFromFull)
                .collect(Collectors.toList());

        return compilations
                .stream()
                .map((compilation -> {
                    List<EventShortDto> eventsShortDto = new ArrayList<>();

                    //среди списка вообще всех allEventsShortDto найти события именно для текущей подборки
                    for (Event event: compilation.getEvents()) {
                        for (EventShortDto eventShortDto: allEventsShortDto) {
                            if (event.getId().equals(eventShortDto.getId())) {
                                eventsShortDto.add(eventShortDto);
                            }
                        }
                    }

                    return CompilationMapper.toCompilationDto(compilation, eventsShortDto);
                }))
                .skip(from)
                .limit(size)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = findCompilationByIdOrThrowException(compId);
        List<EventShortDto> eventsShortDto = eventService.getFullDtoForAllEvents(compilation.getEvents())
                .stream()
                .map(EventMapper::toEventShortDtoFromFull)
                .collect(Collectors.toList());
        return CompilationMapper.toCompilationDto(compilation, eventsShortDto);
    }

    @Override
    public CompilationDto addNewCompilation(NewCompilationDto newCompilationDto) {

        //создать новый объект
        Compilation newCompilation = new Compilation();
        newCompilation.setTitle(newCompilationDto.getTitle());
        newCompilation.setPinned(newCompilationDto.isPinned());

        //получить категории по списку
        List<EventShortDto> eventsShortDto = new ArrayList<>();
        if (!newCompilationDto.getEvents().isEmpty()) {
            List<Event> events  = eventRepository.findAllById(newCompilationDto.getEvents());
            newCompilation.setEvents(events);
            eventsShortDto.addAll(
                    eventService.getFullDtoForAllEvents(events)
                            .stream()
                            .map(EventMapper::toEventShortDtoFromFull)
                            .collect(Collectors.toList())
            );
        }

        return CompilationMapper.toCompilationDto(compilationRepository.save(newCompilation), eventsShortDto);
    }

    @Override
    public void deleteCompilation(Long compId) {
       compilationRepository.deleteById(compId);
    }

    @Override
    public void deleteEventFromCompilation(Long compId, Long eventId) {
        //Удалить событие из подборки
        //найти подборку для обновления
        Compilation compilation = findCompilationByIdOrThrowException(compId);

        //найти событие для удаления из подборки
        Event event = findEventByIdOrThrowException(eventId);

        //проверить, что это событие есть в этой подборке
        if (!compilation.getEvents().contains(event)) {
            throw new CompilationNotContainEventForDeleteException(compilation, event);
        }

        //удалить событие из подборки
        compilation.getEvents().remove(event);
        compilationRepository.save(compilation);
    }

    @Override
    public void addEventToCompilation(Long compId, Long eventId) {
        //Добавить событие в подборку
        Compilation compilation = findCompilationByIdOrThrowException(compId);

        //найти событие для добавления в подборку
        Event event = findEventByIdOrThrowException(eventId);

        //проверить, что этого события еще нет в этой подборке
        if (compilation.getEvents().contains(event)) {
            throw new CompilationAlreadyExistsEventException(compilation, event);
        }

        //добавить событие в подборку
        compilation.getEvents().add(event);
        compilationRepository.save(compilation);
    }

    @Override
    public void unpinCompilation(Long compId) {
        //Открепить подборку на главной странице
        Compilation compilation = findCompilationByIdOrThrowException(compId);

        compilation.setPinned(false);
        compilationRepository.save(compilation);
    }

    @Override
    public void pinCompilation(Long compId) {
        //Закрепить подборку на главной странице
        Compilation compilation = findCompilationByIdOrThrowException(compId);

        compilation.setPinned(true);
        compilationRepository.save(compilation);
    }

    private Compilation findCompilationByIdOrThrowException(Long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new CompilationNotFoundException(compId));
    }

    private Event findEventByIdOrThrowException(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
    }
}
