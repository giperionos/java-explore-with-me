package ru.practicum.explore.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.dto.CategoryDto;
import ru.practicum.explore.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping(path = "/categories")
@RequiredArgsConstructor
@Slf4j
public class PublicCategoryController {

    private final CategoryService service;

    @GetMapping
    public List<CategoryDto> getAllCategories(
            @RequestParam(name = "from", defaultValue = "0")  Integer from,
            @RequestParam(name = "size", defaultValue = "10")  Integer size) {
        log.info("PublicCategoryController: Получен {} запрос с параметрами: from = {} size = {}", "GET", from, size);
        return service.getAllCategories(from, size);
    }

    @GetMapping("/{catId}")
    public CategoryDto getCategoryById(@PathVariable Long catId) {
        log.info("PublicCategoryController: Получен {} запрос с параметрами: catId = {}", "GET /{catId}", catId);
        return service.getCategoryById(catId);
    }
}
