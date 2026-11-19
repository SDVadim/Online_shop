package org.example.service;

import org.example.controller.dto.CategoryRequest;
import org.example.controller.dto.CategoryResponse;
import org.example.exception.CategoryNotEmptyException;
import org.example.mapper.CategoryMapper;
import org.example.repository.CategoryRepository;
import org.example.repository.Entities.CategoryEntity;
import org.example.repository.ProductRepository;
import org.example.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.example.exception.ResourceNotFoundException.Resource;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(
        CategoryRepository categoryRepository,
        ProductRepository productRepository,
        CategoryMapper categoryMapper
    ) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.categoryMapper = categoryMapper;
    }

    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll().stream()
            .map(categoryMapper::toResponse)
            .toList();
    }

    public CategoryResponse get(Long id) {
        CategoryEntity entity = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(Resource.CATEGORY, id));
        return categoryMapper.toResponse(entity);
    }

    public Long create(CategoryRequest categoryRequest) {
        CategoryEntity categoryEntity = categoryMapper.toEntity(categoryRequest);
        categoryEntity = categoryRepository.save(categoryEntity);
        return categoryEntity.getId();
    }

    public CategoryResponse update(Long id, CategoryRequest categoryRequest) {
        CategoryEntity entity = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(Resource.CATEGORY, id));
        entity.update(categoryRequest.name()); //todo: сделать независимым от полей

        categoryRepository.save(entity);
        return categoryMapper.toResponse(entity);
    }

    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException(Resource.CATEGORY, id);
        }
        boolean isUsed = productRepository.existsByCategoryId(id);
        if (!isUsed) {
            categoryRepository.deleteById(id);
        } else {
            throw new CategoryNotEmptyException("Category with ID: " + id + " already used");
        }
    }
}
