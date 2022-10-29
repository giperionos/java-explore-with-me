package ru.practicum.explore.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.explore.dto.CategoryDto;
import ru.practicum.explore.dto.NewCategoryDto;
import ru.practicum.explore.model.Category;
import ru.practicum.explore.repository.CategoryRepository;
import ru.practicum.explore.repository.EventRepository;
import ru.practicum.explore.service.CategoryService;
import ru.practicum.explore.service.exceptions.CategoryNotFoundException;
import ru.practicum.explore.service.exceptions.CategoryRestrictDeleteException;
import ru.practicum.explore.service.mapper.CategoryMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    public List<CategoryDto> getAllCategories(Integer from, Integer size) {
        return categoryRepository.findAll(PageRequest.of(from / size, size, Sort.by("id").ascending()))
                .stream()
                .map(CategoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategoryById(Long catId) {
        return CategoryMapper.toCategoryDto(findCategoryByIdOrThrowException(catId));
    }

    @Override
    public CategoryDto addNewCategory(NewCategoryDto newCategoryDto) {
        Category categoryForSave = CategoryMapper.toCategory(newCategoryDto);
        return CategoryMapper.toCategoryDto(categoryRepository.save(categoryForSave));
    }

    @Override
    public CategoryDto updateCategory(CategoryDto categoryDto) {
        //сначала нужно убедиться, что категория, которая обновляется вообще есть
        Category categoryForUpdate = findCategoryByIdOrThrowException(categoryDto.getId());

        if (!categoryDto.getName().isBlank()) {
            categoryForUpdate.setName(categoryDto.getName());
        }

        return CategoryMapper.toCategoryDto(categoryRepository.save(categoryForUpdate));
    }

    @Override
    public void deleteCategory(Long categoryId) {
        //сначала нужно убедиться, что категория, которая обновляется вообще есть
        Category categoryForDelete = findCategoryByIdOrThrowException(categoryId);

        //с категорией не должно быть связано ни одного события, иначе ошибка
        if (!eventRepository.findByCategory_Id(categoryId).isEmpty()) {
            throw new CategoryRestrictDeleteException(categoryId);
        }

        categoryRepository.delete(categoryForDelete);
    }

    @Override
    public Category findCategoryByIdOrThrowException(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new CategoryNotFoundException(catId));
    }

    @Override
    public List<Category> getCategoryByIds(List<Long> ids) {
        return categoryRepository.findAllById(ids);
    }
}
