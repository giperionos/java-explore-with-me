package ru.practicum.explore.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.dto.CompilationDto;
import ru.practicum.explore.dto.NewCompilationDto;
import ru.practicum.explore.service.CompilationService;

@RestController
@RequestMapping(path = "/admin/compilations")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AdminCompilationController {

    private final CompilationService service;

    @PostMapping
    public CompilationDto addNewCompilation(@Validated @RequestBody NewCompilationDto newCompilationDto) {
        log.info("AdminCompilationController: Получен {} запрос с параметрами: newCompilationDto = {}", "POST", newCompilationDto);
        return service.addNewCompilation(newCompilationDto);
    }

    @DeleteMapping("/{compId}")
    public void deleteCompilation(@PathVariable Long compId) {
        log.info("AdminCompilationController: Получен {} запрос с параметрами: compId = {}", "DELETE /{compId}", compId);
        service.deleteCompilation(compId);
    }

    @DeleteMapping("/{compId}/events/{eventId}")
    public void deleteEventFromCompilation(@PathVariable Long compId, @PathVariable Long eventId) {

        log.info("AdminCompilationController: Получен {} запрос с параметрами: compId = {} eventId = {}",
                "DELETE /{compId}/events/{eventId}", compId, eventId);

        service.deleteEventFromCompilation(compId, eventId);
    }

    @PatchMapping("/{compId}/events/{eventId}")
    public void addEventToCompilation(@PathVariable Long compId, @PathVariable Long eventId) {

        log.info("AdminCompilationController: Получен {} запрос с параметрами: compId = {} eventId = {}",
                "PATCH /{compId}/events/{eventId}", compId, eventId);

        service.addEventToCompilation(compId, eventId);
    }

    @DeleteMapping("/{compId}/pin")
    public void unpinCompilation(@PathVariable Long compId) {
        log.info("AdminCompilationController: Получен {} запрос с параметрами: compId = {}", "DELETE /{compId}/pin", compId);
        service.unpinCompilation(compId);
    }

    @PatchMapping("/{compId}/pin")
    public void pinCompilation(@PathVariable Long compId) {
        log.info("AdminCompilationController: Получен {} запрос с параметрами: compId = {}", "PATCH /{compId}/pin", compId);
        service.pinCompilation(compId);
    }
}
