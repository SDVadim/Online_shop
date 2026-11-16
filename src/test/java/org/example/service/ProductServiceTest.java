package org.example.service;

import org.example.controller.dto.CategoryRequest;
import org.example.controller.dto.ProductRequest;
import org.example.controller.dto.ProductResponse;
import org.example.exception.ResourceNotFoundException;
import org.example.mapper.ProductMapper;
import org.example.repository.CategoryRepository;
import org.example.repository.Entities.CategoryEntity;
import org.example.repository.Entities.ProductEntity;
import org.example.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductServiceTest {
    private ProductRequest productRequest;
    private ProductResponse productResponse;
    private ProductEntity productEntity;
    private ProductEntity productEntitySaved;
    private CategoryEntity categoryEntity;

    private ProductService productService;
    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;
    private ProductMapper productMapper;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        categoryRepository = mock(CategoryRepository.class);
        productMapper = mock(ProductMapper.class);
        productService = new ProductService(productRepository, categoryRepository, productMapper);


        categoryEntity = new CategoryEntity(1L, "category");
        productRequest = new ProductRequest("example", new BigDecimal(2000), 1L);
        productResponse = ProductResponse.builder()
            .id(100L)
            .name("example")
            .categoryId(1L)
            .price(new BigDecimal(2000))
            .build();
        productEntity = ProductEntity.builder()
            .category(categoryEntity)
            .name(productRequest.name())
            .price(productRequest.price())
            .build();
        productEntitySaved = ProductEntity.builder()
            .id(100L)
            .category(categoryEntity)
            .name(productRequest.name())
            .price(productRequest.price())
            .build();
    }

    @Test
    void deleteProductWhenProductDoesNotExist() {
        Long uselessId = 1000L;

        when(productRepository.existsById(uselessId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> productService.deleteProduct(uselessId));

        verify(productRepository, never()).deleteById(uselessId);
    }

    @Test
    void deleteProductWhenProductExists() {
        Long id = 1L;
        when(productRepository.existsById(id)).thenReturn(true);
        productService.deleteProduct(id);
        verify(productRepository).deleteById(id);
    }

    @Test
    void addProductWhenCategoryDoesNotExist() {
        when(categoryRepository.existsById(anyLong())).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> productService.addProduct(productRequest));

        verify(productRepository, never()).save(any());
    }

    @Test
    void addProductWhenCategoryExist() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(categoryEntity));
        when(productMapper.toEntity(productRequest, categoryEntity)).thenReturn(productEntity);
        when(productRepository.save(productEntity)).thenReturn(productEntitySaved);

        Long id = productService.addProduct(productRequest);
        assertEquals(productEntitySaved.getId(), id);

        verify(productRepository).save(any());
    }

    @Test
    void getByIdProductExists() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(productEntitySaved));
        when(productMapper.toResponse(productEntitySaved)).thenReturn(productResponse);
        assertEquals(productResponse, productService.getById(100L));
    }

    @Test
    void getByIdProductDoesNotExists() {
        when(productRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getById(100L));

        verify(productMapper, never()).toResponse(any());
    }

    @Test
    void getAll() {
        when(productRepository.findAll()).thenReturn(List.of(productEntitySaved, productEntitySaved));
        when(productMapper.toResponse(productEntitySaved)).thenReturn(productResponse);

        List<ProductResponse> responses = productService.getAll();
        assertEquals(productResponse, responses.get(0));
        assertEquals(2, responses.size());
        verify(productRepository).findAll();
    }

    @Test
    void updateCategoryDoesNotExist() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.update(100L, productRequest));
        verify(productRepository, never()).findById(100L);
        verify(productMapper, never()).toResponse(any());
    }

    @Test
    void updateProductDoesNotExist() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(categoryEntity));
        when(productRepository.findById(100L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.update(100L, productRequest));
        verify(categoryRepository).findById(1L);
        verify(productMapper, never()).toResponse(any());
    }

    @Test
    void updateAllExists() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(categoryEntity));
        when(productRepository.findById(100L)).thenReturn(Optional.of(productEntitySaved));
        when(productRepository.save(productEntitySaved)).thenReturn(productEntitySaved);
        when(productMapper.toResponse(productEntitySaved)).thenReturn(productResponse);
        assertEquals(productResponse, productService.update(100L, productRequest));
        verify(productRepository).findById(100L);
        verify(categoryRepository).findById(1L);
        verify(productRepository).save(any());
    }

    @Test
    void getAllProductsByCategoryIdCategoryDoesNotExist() {
        when(categoryRepository.existsById(1L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> productService.getAllProductsByCategoryId(categoryEntity.getId()));
        verify(productRepository, never()).findByCategoryId(any());
    }

    @Test
    void getAllProductsByCategoryIdCategoryExist() {
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(productRepository.findByCategoryId(1L)).thenReturn(List.of(productEntitySaved, productEntitySaved));
        when(productMapper.toResponse(productEntitySaved)).thenReturn(productResponse);
        List<ProductResponse> responses = productService.getAllProductsByCategoryId(1L);
        assertEquals(productResponse, responses.get(0));
        assertEquals(2, responses.size());
        verify(productRepository).findByCategoryId(1L);
    }
}