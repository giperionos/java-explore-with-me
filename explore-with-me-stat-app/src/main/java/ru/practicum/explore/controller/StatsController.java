package ru.practicum.explore.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.EndpointViewParamsHelper;
import ru.practicum.explore.dto.EndpointHitDto;
import ru.practicum.explore.dto.EndpointViewDto;
import ru.practicum.explore.validator.StartDateBeforeEndDateValidator;
import ru.practicum.explore.service.StatsService;

import java.util.Arrays;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
public class StatsController {

    private final StatsService service;

    @PostMapping("/hit")
    public EndpointHitDto addHitToLog(@Validated @RequestBody EndpointHitDto endpointHitDto) {
        log.info("Get POST Request /hit for {}", endpointHitDto);
        return service.addHitToLog(endpointHitDto);
    }

    @GetMapping("/stats")
    public List<EndpointViewDto> getEndpointStatsByParams(
            @RequestParam(name = "start") String encodedStart,
            @RequestParam(name = "end") String encodedEnd,
            @RequestParam(name = "uris", required = false) String[] uriArray,
            @RequestParam(name = "unique", required = false) Boolean unique) {

        log.info("Get GET Request /stats for params: {}, {}, {}, {}", encodedStart, encodedEnd, uriArray, unique);
        EndpointViewParamsHelper params = EndpointViewParamsHelper.ofEncodedValues(encodedStart, encodedEnd, Arrays.asList(uriArray), unique);
        StartDateBeforeEndDateValidator.validate(params.getStart(), params.getEnd());
        return service.getEndpointStatsByParams(params);
    }
}
