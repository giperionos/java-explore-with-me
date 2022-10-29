package ru.practicum.explore.service.impl;

import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.explore.dto.CompilationDto;
import ru.practicum.explore.dto.EventShortDto;
import ru.practicum.explore.dto.NewCompilationDto;
import ru.practicum.explore.model.Compilation;
import ru.practicum.explore.model.Event;
import ru.practicum.explore.model.QCompilation;
import ru.practicum.explore.repository.CompilationRepository;
import ru.practicum.explore.repository.EventRepository;
import ru.practicum.explore.service.CompilationService;
import ru.practicum.explore.service.EventService;
import ru.practicum.explore.service.exceptions.CompilationAlreadyExistsEventException;
import ru.practicum.explore.service.exceptions.CompilationNotContainEventForDeleteException;
import ru.practicum.explore.service.exceptions.CompilationNotFoundException;
import ru.practicum.explore.service.mapper.CompilationMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final EventService eventService;

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {

        List<Compilation> compilations = new ArrayList<>();
        if (pinned != null && pinned) {
            Predicate byPinned = QCompilation.compilation.pinned.eq(pinned);
            compilations.addAll(compilationRepository.findAll(byPinned, Pageable.unpaged()).toList());
        } else {
            compilations.addAll(compilationRepository.findAll());
        }

        return compilations
                .stream()
                .map((compilation -> {
                    List<EventShortDto> eventsShortDto = compilation.getEvents().stream().map(eventService::mapEventToEventShortDto).collect(Collectors.toList());
                    return CompilationMapper.toCompilationDto(compilation, eventsShortDto);
                }))
                .skip(from)
                .limit(size)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = findCompilationByIdOrThrowException(compId);
        List<EventShortDto> eventsShortDto = compilation.getEvents().stream().map(eventService::mapEventToEventShortDto).collect(Collectors.toList());
        return CompilationMapper.toCompilationDto(compilation, eventsShortDto);
    }

    @Override
    public CompilationDto addNewCompilation(NewCompilationDto newCompilationDto) {

        //создать новый объект
        Compilation newCompilation = new Compilation();
        newCompilation.setTitle(newCompilationDto.getTitle());
        newCompilation.setPinned(newCompilationDto.getPinned());

        //получить категории по списку
        List<EventShortDto> eventsShortDto = new ArrayList<>();
        if (!newCompilationDto.getEvents().isEmpty()) {
            List<Event> events  = eventRepository.findAllById(newCompilationDto.getEvents());
            newCompilation.setEvents(events);
            eventsShortDto.addAll(events.stream()
                    .map(eventService::mapEventToEventShortDto)
                    .collect(Collectors.toList()));
        }

        return CompilationMapper.toCompilationDto(compilationRepository.save(newCompilation), eventsShortDto);
    }

    @Override
    public void deleteCompilation(Long compId) {
        //проверить, есть ли такая подборка
       Compilation compilationForDelete = findCompilationByIdOrThrowException(compId);
       compilationRepository.delete(compilationForDelete);
    }

    @Override
    public void deleteEventFromCompilation(Long compId, Long eventId) {
        //Удалить событие из подборки
        //проверить, есть ли такая подборка
        Compilation compilation = findCompilationByIdOrThrowException(compId);

        //убедиться, чта такое событие есть
        Event event = eventService.findEventByIdOrThrowException(eventId);

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

        //убедиться, чта такое событие есть
        Event event = eventService.findEventByIdOrThrowException(eventId);

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

    @Override
    public Compilation findCompilationByIdOrThrowException(Long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new CompilationNotFoundException(compId));
    }
}
