package org.example;

import org.example.DTO.ProductRequest;
import org.example.DTO.ProductResponse;
import org.example.Repository.Entities.ProductEntityExample;
import org.example.Repository.ProductsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private final ProductsRepository productsRepository;


    public ProductService(ProductsRepository repository) {
        this.productsRepository = repository;
    }


    public Long addProduct(ProductRequest product) {
        Long id = productsRepository.addProduct(product.name(), product.price());
        return id;
    }

    public ProductResponse getById(Long id) {
        ProductEntityExample entity = productsRepository.findById(id);
        ProductResponse response = new ProductResponse(entity.getName(), entity.getPrice(), entity.getId());
        return response;
    }

    List<ProductResponse> getAll() {
        return productsRepository.findAll().stream()
            .map(entity -> {
                return new ProductResponse(entity.getName(), entity.getPrice(), entity.getId());
            })
            .toList();


    }

    ProductResponse update(Long id, ProductRequest product) {
        var entity = new ProductEntityExample(product.name(), product.price(), id);
        productsRepository.updateProduct(entity);
        return getById(id);
    }

    void deleteProduct(Long id) {
        productsRepository.deleteProduct(id);
    }


}
