package ru.practicum.explore.service.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.explore.dto.CompilationDto;
import ru.practicum.explore.dto.EventShortDto;
import ru.practicum.explore.model.Compilation;
import ru.practicum.explore.model.Event;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CompilationMapper {

    public static CompilationDto toCompilationDto(Compilation compilation, List<EventShortDto> eventsShortDto) {
        CompilationDto compilationDto = new CompilationDto();
        compilationDto.setId(compilation.getId());
        compilationDto.setTitle(compilation.getTitle());
        compilationDto.setPinned(compilation.isPinned());
        compilationDto.setEvents(eventsShortDto);
        return compilationDto;
    }

    public static Compilation toCompilation(CompilationDto compilationDto, List<Event> events) {
        Compilation compilation = new Compilation();
        compilation.setId(compilationDto.getId());
        compilation.setTitle(compilationDto.getTitle());
        compilation.setPinned(compilationDto.getPinned());
        compilation.setEvents(events);
        return compilation;
    }
}
