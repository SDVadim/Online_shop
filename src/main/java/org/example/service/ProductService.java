package org.example.service;

import org.example.controller.dto.ProductRequest;
import org.example.controller.dto.ProductResponse;
import org.example.mapper.ProductMapper;
import org.example.repository.CartItemRepository;
import org.example.repository.CategoryRepository;
import org.example.repository.Entities.CategoryEntity;
import org.example.repository.Entities.ProductEntity;
import org.example.repository.ProductRepository;
import org.example.exception.ResourceNotFoundException;
import org.example.exception.ResourceNotFoundException.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CartItemRepository cartItemRepository;

    private final ProductMapper productMapper;


    public ProductService(
        ProductRepository repository,
        CategoryRepository categoryRepository, CartItemRepository cartItemRepository,
        ProductMapper productMapper
    ) {
        this.productRepository = repository;
        this.categoryRepository = categoryRepository;
        this.cartItemRepository = cartItemRepository;
        this.productMapper = productMapper;
    }


    @Transactional
    public Long addProduct(ProductRequest product) {
        CategoryEntity categoryEntity = categoryRepository.findById(product.categoryId())
            .orElseThrow(() -> new ResourceNotFoundException(Resource.CATEGORY, product.categoryId()));
        ProductEntity entity = productMapper.toEntity(product, categoryEntity);
        return productRepository.save(entity).getId();
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        ProductEntity entity = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(Resource.PRODUCT, id));
        return productMapper.toResponse(entity);

    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAll() {
        return productRepository.findAll().stream()
            .map(productMapper::toResponse)
            .toList();
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest product) {
        CategoryEntity categoryEntity = categoryRepository.findById(product.categoryId())
            .orElseThrow(() -> new ResourceNotFoundException(Resource.CATEGORY, product.categoryId()));

        ProductEntity productEntity = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(Resource.PRODUCT, id));

        productEntity.update(product.name(), product.price(), categoryEntity);
        return productMapper.toResponse(productEntity);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException(Resource.PRODUCT, id);
        }
        cartItemRepository.deleteAllByProductId(id); //todo: подумать как сделать по другому. мб сделать статус товара отсутствует или что-то в это роде
        productRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProductsByCategoryId(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException(Resource.CATEGORY, id);
        }
        return productRepository.findByCategoryId(id).stream()
            .map(productMapper::toResponse)
            .toList();
    }
}
