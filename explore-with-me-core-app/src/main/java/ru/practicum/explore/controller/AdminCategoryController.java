package ru.practicum.explore.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.dto.CategoryDto;
import ru.practicum.explore.dto.NewCategoryDto;
import ru.practicum.explore.service.CategoryService;

@RestController
@RequestMapping(path = "/admin/categories")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AdminCategoryController {

    private final CategoryService service;

    @PostMapping
    public CategoryDto addNewCategory(@Validated @RequestBody NewCategoryDto newCategoryDto) {
        log.info("AdminCategoryController: Получен {} запрос с параметрами: newCategoryDto = {}", "POST", newCategoryDto);
        return service.addNewCategory(newCategoryDto);
    }

    @PatchMapping
    public CategoryDto updateCategory(@Validated @RequestBody CategoryDto categoryDto) {
        log.info("AdminCategoryController: Получен {} запрос с параметрами: categoryDto = {}", "PATCH", categoryDto);
        return service.updateCategory(categoryDto);
    }

    @DeleteMapping("/{catId}")
    public void deleteCategory(@PathVariable(name = "catId") Long categoryId) {
        log.info("AdminCategoryController: Получен {} запрос с параметрами: categoryId = {}", "DELETE", categoryId);
        service.deleteCategory(categoryId);
    }
}
