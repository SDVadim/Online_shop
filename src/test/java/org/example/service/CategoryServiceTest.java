package org.example.service;

import org.example.controller.dto.CategoryRequest;
import org.example.controller.dto.CategoryResponse;
import org.example.exception.CategoryNotEmptyException;
import org.example.exception.ResourceNotFoundException;
import org.example.mapper.CategoryMapper;
import org.example.repository.CategoryRepository;
import org.example.repository.Entities.CategoryEntity;
import org.example.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CategoryServiceTest {
    private CategoryRequest categoryRequest;
    private CategoryResponse categoryResponse;
    private CategoryEntity categoryEntity;
    private CategoryEntity categoryEntitySaved;

    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;
    private CategoryMapper categoryMapper;
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        categoryRepository = mock(CategoryRepository.class);
        categoryMapper = mock(CategoryMapper.class);
        categoryService = new CategoryService(categoryRepository, productRepository, categoryMapper);
    }

    void createTestFields() {
        categoryRequest = new CategoryRequest("category");
        categoryResponse = new CategoryResponse(1L, "category");

        categoryEntity = CategoryEntity.builder().name(categoryRequest.name()).build();
        categoryEntitySaved = CategoryEntity.builder().id(1L).name(categoryRequest.name()).build();
    }


    @Test
    void getAll() {
        createTestFields();
        when(categoryRepository.findAll()).thenReturn(List.of(categoryEntitySaved, categoryEntitySaved));
        when(categoryMapper.toResponse(categoryEntitySaved)).thenReturn(categoryResponse);
        List<CategoryResponse> responses = categoryService.getAll();
        assertEquals(categoryResponse, responses.get(0));
        assertEquals(2, responses.size());
    }

    @Test
    void getCategoryDoesNotExist() {
        when(categoryRepository.findById(1000L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> categoryService.get(1000L));
        verify(categoryMapper, never()).toResponse(any());
    }

    @Test
    void getCategory() {
        createTestFields();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(categoryEntitySaved));
        when(categoryMapper.toResponse(categoryEntitySaved)).thenReturn(categoryResponse);
        assertEquals(categoryResponse, categoryService.get(1L));
    }

    @Test
    void create() {
        createTestFields();
        when(categoryMapper.toEntity(categoryRequest)).thenReturn(categoryEntity);
        when(categoryRepository.save(categoryEntity)).thenReturn(categoryEntitySaved);
        Long id = categoryService.create(categoryRequest);
        assertEquals(1L, id);
    }

    @Test
    void update() {
        createTestFields();
        CategoryRequest updateCategoryRequest = new CategoryRequest("update category");
        CategoryResponse expectedResponse = new CategoryResponse(1L, "update category");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(categoryEntitySaved));
        when(categoryMapper.toResponse(categoryEntitySaved)).thenReturn(expectedResponse);

        CategoryResponse actual = categoryService.update(1L, updateCategoryRequest);

        assertEquals(updateCategoryRequest.name(), categoryEntitySaved.getName());
        assertEquals(1L, categoryEntitySaved.getId());
        verify(categoryRepository).save(categoryEntitySaved);
        assertEquals(expectedResponse, actual);
    }

    @Test
    void updateCategoryDoesNotExist() {
        createTestFields();

        when(categoryRepository.findById(1000L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.update(1000L, categoryRequest));
        verify(categoryMapper, never()).toResponse(any());
    }

    @Test
    void deleteCategoryDoesNotExist() {
        when(categoryRepository.existsById(1000L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> categoryService.delete(1000L));
        verify(productRepository, never()).existsByCategoryId(1000L);
        verify(categoryRepository, never()).deleteById(1000L);
    }

    @Test
    void deleteCategoryAlreadyInUse() {
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(productRepository.existsByCategoryId(1L)).thenReturn(true);
        assertThrows(CategoryNotEmptyException.class, () -> categoryService.delete(1L));
    }

    @Test
    void delete() {
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(productRepository.existsByCategoryId(1L)).thenReturn(false);
        categoryService.delete(1L);
        verify(categoryRepository).deleteById(1L);
    }
}