package org.example.service;

import org.example.exception.ResourceNotFoundException;
import org.example.mapper.ProductMapper;
import org.example.repository.CategoryRepository;
import org.example.repository.ProductRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductServiceTest {

    @Test
    void deleteProductWhenProductDoesNotExist() {
        ProductRepository productRepository = mock(ProductRepository.class);
        CategoryRepository categoryRepository = mock(CategoryRepository.class);
        ProductMapper productMapper = mock(ProductMapper.class);

        ProductService productService = new ProductService(productRepository, categoryRepository, productMapper);

        Long uselessId = 1000L;

        when(productRepository.existsById(uselessId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> productService.deleteProduct(uselessId));

        verify(productRepository, never()).deleteById(uselessId);
    }

    @Test
    void deleteProductWhenProductExists() {
        ProductRepository productRepository = mock(ProductRepository.class);
        CategoryRepository categoryRepository = mock(CategoryRepository.class);
        ProductMapper productMapper = mock(ProductMapper.class);
        ProductService productService = new ProductService(productRepository, categoryRepository, productMapper);

        Long id = 1L;
        when(productRepository.existsById(id)).thenReturn(true);
        productService.deleteProduct(id);
        verify(productRepository).deleteById(id);
    }
}