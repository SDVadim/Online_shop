package org.example.service;

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
    private CategoryEntity categoryEntitySaved;

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
    }

    void createTestFields() {
        categoryEntitySaved = new CategoryEntity(1L, "category");
        productRequest = new ProductRequest("example", new BigDecimal(2000), 1L);
        productResponse = ProductResponse.builder()
            .id(100L)
            .name("example")
            .categoryId(1L)
            .price(new BigDecimal(2000))
            .build();
        productEntity = ProductEntity.builder()
            .category(categoryEntitySaved)
            .name(productRequest.name())
            .price(productRequest.price())
            .build();
        productEntitySaved = ProductEntity.builder()
            .id(100L)
            .category(categoryEntitySaved)
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
        createTestFields();
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.addProduct(productRequest));

        verify(productRepository, never()).save(any());
    }

    @Test
    void addProductWhenCategoryExist() {
        createTestFields();
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(categoryEntitySaved));
        when(productMapper.toEntity(productRequest, categoryEntitySaved)).thenReturn(productEntity);
        when(productRepository.save(productEntity)).thenReturn(productEntitySaved);

        Long id = productService.addProduct(productRequest);
        assertEquals(productEntitySaved.getId(), id);

        verify(productRepository).save(any());
    }

    @Test
    void getByIdProductExists() {
        createTestFields();
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
        createTestFields();
        when(productRepository.findAll()).thenReturn(List.of(productEntitySaved, productEntitySaved));
        when(productMapper.toResponse(productEntitySaved)).thenReturn(productResponse);

        List<ProductResponse> responses = productService.getAll();
        assertEquals(productResponse, responses.get(0));
        assertEquals(2, responses.size());
    }

    @Test
    void updateCategoryDoesNotExist() {
        createTestFields();
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.update(100L, productRequest));
        verify(productRepository, never()).findById(100L);
        verify(productMapper, never()).toResponse(any());
    }

    @Test
    void updateProductDoesNotExist() {
        createTestFields();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(categoryEntitySaved));
        when(productRepository.findById(100L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.update(100L, productRequest));
        verify(categoryRepository).findById(1L);
        verify(productMapper, never()).toResponse(any());
    }

    @Test
    void updateAllExists() {
        createTestFields();
        ProductRequest updateProductRequest = new ProductRequest("new example", new BigDecimal(3000), 1L);
        ProductResponse expectedResponse = new ProductResponse(100L, "new example", new BigDecimal(3000), 1L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(categoryEntitySaved));
        when(productRepository.findById(100L)).thenReturn(Optional.of(productEntitySaved));
        when(productMapper.toResponse(productEntitySaved)).thenReturn(expectedResponse);

        ProductResponse actual = productService.update(100L, updateProductRequest);

        verify(productRepository).save(productEntitySaved);
        assertEquals(expectedResponse, actual);
    }

    @Test
    void getAllProductsByCategoryIdCategoryDoesNotExist() {
        createTestFields();
        when(categoryRepository.existsById(1L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> productService.getAllProductsByCategoryId(categoryEntitySaved.getId()));
        verify(productRepository, never()).findByCategoryId(any());
    }

    @Test
    void getAllProductsByCategoryIdCategoryExist() {
        createTestFields();
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(productRepository.findByCategoryId(1L)).thenReturn(List.of(productEntitySaved, productEntitySaved));
        when(productMapper.toResponse(productEntitySaved)).thenReturn(productResponse);
        List<ProductResponse> responses = productService.getAllProductsByCategoryId(1L);
        assertEquals(productResponse, responses.get(0));
        assertEquals(2, responses.size());
    }
}