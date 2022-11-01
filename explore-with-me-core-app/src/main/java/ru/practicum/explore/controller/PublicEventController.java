package ru.practicum.explore.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.dto.UserEventFilter;
import ru.practicum.explore.dto.EventFullDto;
import ru.practicum.explore.dto.EventShortDto;
import ru.practicum.explore.service.EventService;
import ru.practicum.explore.dto.EventSortType;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping(path = "/events")
@RequiredArgsConstructor
@Slf4j
public class PublicEventController {

    private final EventService service;

    @GetMapping
    public List<EventShortDto> getEventsByParams(
            @RequestParam(name = "text", required = false) String textSearch,
            @RequestParam(name = "categories", required = false) List<Long> categoriesIds,
            @RequestParam(name = "paid", required = false) Boolean paid,
            @RequestParam(name = "rangeStart", required = false) String rangeStartEncoded,
            @RequestParam(name = "rangeEnd", required = false) String rangeEndEncoded,
            @RequestParam(name = "onlyAvailable", required = false, defaultValue = "false") Boolean onlyAvailableByRequestLimit,
            @RequestParam(name = "sort", required = false) String sortStr,
            @RequestParam(name = "from", required = false, defaultValue = "0")  Integer from,
            @RequestParam(name = "size", required = false, defaultValue = "10")  Integer size,
            HttpServletRequest request) {

        log.info("PublicEventController: Получен GET запрос с параметрами: "
                + "text = {}"
                + "categories = {}"
                + "paid = {}"
                + "rangeStartStr = {}"
                + "rangeEndStr = {}"
                + "onlyAvailableByRequestLimit = {}"
                + "sortStr = {}"
                + "from = {}"
                + "size = {}",
                textSearch,
                categoriesIds,
                paid,
                rangeStartEncoded,
                rangeEndEncoded,
                onlyAvailableByRequestLimit,
                sortStr,
                from,
                size);

        UserEventFilter filter = UserEventFilter.builder()
                .textSearch(textSearch)
                .categoriesIds(categoriesIds)
                .paid(paid)
                .rangeStartEncoded(rangeStartEncoded)
                .rangeEndEncoded(rangeEndEncoded)
                .onlyAvailableByRequestLimit(onlyAvailableByRequestLimit)
                .sortType(sortStr.isBlank() ? null : EventSortType.from(sortStr))
                .from(from)
                .size(size)
                .build();

        return service.getEventsByFilter(filter, request);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventWithFullInfoById(@PathVariable Long eventId, HttpServletRequest request) {
        log.info("PublicEventController: Получен {} запрос с параметрами: eventId = {}", "GET /{eventId}", eventId);
        return service.getEventWithFullInfoById(eventId, request);
    }
}
