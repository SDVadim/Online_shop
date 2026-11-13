package org.example.service;

import org.example.controller.dto.ProductRequest;
import org.example.controller.dto.ProductResponse;
import org.example.mapper.ProductMapper;
import org.example.repository.CategoryRepository;
import org.example.repository.Entities.CategoryEntity;
import org.example.repository.Entities.ProductEntity;
import org.example.repository.ProductRepository;
import org.example.repository.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    private final ProductMapper productMapper;


    public ProductService(
        ProductRepository repository,
        CategoryRepository categoryRepository,
        ProductMapper productMapper
    ) {
        this.productRepository = repository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
    }


    public Long addProduct(ProductRequest product) {

        CategoryEntity categoryEntity = categoryRepository.findById(product.categoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category with ID:" + product.categoryId() + " not found"));
        ProductEntity entity = productMapper.toEntity(product, categoryEntity);
        Long id = productRepository.save(entity).getId();
        return id;
    }

    public ProductResponse getById(Long id) {
        ProductEntity entity = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product with ID:" + id + " not found"));

        return productMapper.toResponse(entity);

    }

    public List<ProductResponse> getAll() {
        return productRepository.findAll().stream()
            .map(productMapper::toResponse)
            .toList();
    }

    public ProductResponse update(Long id, ProductRequest product) {
        CategoryEntity categoryEntity = categoryRepository.findById(product.categoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category with ID:" + product.categoryId() + " not found"));

        ProductEntity productEntity = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product with ID:" + id + " not found"));

        productEntity.update(product.name(), product.price(), categoryEntity);
        productRepository.save(productEntity);

        return productMapper.toResponse(productEntity);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public List<ProductResponse> getAllProductsByCategoryId(Long id) {
        return productRepository.findByCategoryId(id).stream()
            .map(productMapper::toResponse)
            .toList();
    }
}
