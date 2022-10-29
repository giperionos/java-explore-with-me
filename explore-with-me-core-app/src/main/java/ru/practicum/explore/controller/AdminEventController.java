package ru.practicum.explore.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.dto.AdminEventFilter;
import ru.practicum.explore.dto.AdminUpdateEventRequestDto;
import ru.practicum.explore.dto.EventFullDto;
import ru.practicum.explore.dto.EventState;
import ru.practicum.explore.service.EventService;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/admin/events")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AdminEventController {

    private final EventService service;

    @GetMapping
    public List<EventFullDto> getEventsWithFullInfoByParams(
            @RequestParam(name = "users") Long[] usersIds,
            @RequestParam(name = "states") String[] states,
            @RequestParam(name = "categories") Long[] categoriesIds,
            @RequestParam(name = "rangeStart") String rangeStartEncoded,
            @RequestParam(name = "rangeEnd") String rangeEndEncoded,
            @RequestParam(name = "from", defaultValue = "0")  Integer from,
            @RequestParam(name = "size", defaultValue = "10")  Integer size) {

        log.info("AdminEventController: Получен GET запрос с параметрами: "
                + "users = {}"
                + "states = {}"
                + "categories = {}"
                + "rangeStart = {}"
                + "rangeEnd = {}"
                + "from = {}"
                + "size = {}",
                usersIds,
                states,
                categoriesIds,
                rangeStartEncoded,
                rangeEndEncoded,
                from,
                size);

        AdminEventFilter filter = AdminEventFilter.builder()
                .usersIds(usersIds)
                .states(states.length == 0 ? null : Arrays.stream(states).map(EventState::from).collect(Collectors.toList()))
                .categoriesIds(categoriesIds)
                .rangeStartEncoded(rangeStartEncoded)
                .rangeEndEncoded(rangeEndEncoded)
                .from(from)
                .size(size)
                .build();

        return service.getEventsWithFullInfoByFilter(filter);
    }

    @PutMapping("/{eventId}")
    public EventFullDto editEvent(
            @PathVariable Long eventId,
            @Validated @RequestBody AdminUpdateEventRequestDto adminUpdateEventRequestDto) {

        log.info("AdminEventController: Получен {} запрос с параметрами: eventId = {} adminUpdateEventRequestDto = {}",
                "PUT /{eventId}", eventId, adminUpdateEventRequestDto);

        return service.editEvent(eventId, adminUpdateEventRequestDto);
    }

    @PatchMapping("/{eventId}/publish")
    public EventFullDto publishEvent(@PathVariable Long eventId) {
        log.info("AdminEventController: Получен {} запрос с параметрами: eventId = {}",
                "PATCH /{eventId}/publish", eventId);

        return service.publishEvent(eventId);
    }

    @PatchMapping("/{eventId}/reject")
    public EventFullDto rejectEvent(@PathVariable Long eventId) {
        log.info("AdminEventController: Получен {} запрос с параметрами: eventId = {}",
                "PATCH /{eventId}/reject", eventId);

        return service.rejectEvent(eventId);
    }
}
