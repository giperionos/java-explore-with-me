package ru.practicum.explore.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.dto.CompilationDto;
import ru.practicum.explore.service.CompilationService;

import java.util.List;

@RestController
@RequestMapping(path = "/compilations")
@RequiredArgsConstructor
@Slf4j
public class PublicCompilationController {

    private final CompilationService service;

    @GetMapping
    public List<CompilationDto> getCompilations(
            @RequestParam(name = "pinned", required = false) Boolean pinned,
            @RequestParam(name = "from", defaultValue = "0")  Integer from,
            @RequestParam(name = "size", defaultValue = "10")  Integer size) {
        log.info("PublicCompilationController: Получен {} запрос с параметрами: pinned ={} from = {} size = {}",
                "GET", pinned, from, size);
        return service.getCompilations(pinned, from, size);
    }

    @GetMapping("/{compId}")
    public CompilationDto getCompilationById(@PathVariable Long compId) {
        log.info("PublicCompilationController: Получен {} запрос с параметрами: compId = {}", "GET /{compId}", compId);
        return service.getCompilationById(compId);
    }
}
