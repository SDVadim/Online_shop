package org.example.service;

import org.example.controller.dto.ProductRequest;
import org.example.controller.dto.ProductResponse;
import org.example.repository.Entities.ProductEntity;
import org.example.repository.ProductsRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private final ProductsRepository productsRepository;


    public ProductService(ProductsRepository repository) {
        this.productsRepository = repository;
    }


    public Long addProduct(ProductRequest product) {
        ProductEntity entity = create(product.name(), product.price());
        Long id = productsRepository.save(entity).getId();
        return id;
    }

    public ProductResponse getById(Long id) {
        Optional<ProductEntity> entity = productsRepository.findById(id);
        if (entity.isPresent()) {

            ProductResponse response = new ProductResponse(entity.get().getId(), entity.get().getName(), entity.get().getPrice());
            return response;
        } else {
            return null;
        }
    }

    public List<ProductResponse> getAll() {
        return productsRepository.findAll().stream()
            .map(entity -> {
                return new ProductResponse(entity.getId(), entity.getName(), entity.getPrice());
            })
            .toList();
    }

    public ProductResponse update(Long id, ProductRequest product) {
        var entity = new ProductEntity(id ,product.name(), product.price());
        productsRepository.save(entity);
        return getById(id);
    }

    public void deleteProduct(Long id) {
        productsRepository.deleteById(id);
    }

    public ProductEntity create(String productName, BigDecimal price) {
        return ProductEntity.builder()
            .name(productName)
            .price(price)
            .build();
    }
}
