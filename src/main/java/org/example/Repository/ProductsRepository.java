package org.example.Repository;

import org.example.Repository.Entities.ProductEntityExample;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

public interface ProductsRepository {
  List<ProductEntityExample> findAll();

  ProductEntityExample findById(Long id);

  Long addProduct(String productName, BigDecimal price);

  void updateProduct(ProductEntityExample product);

  void deleteProduct(Long Id );
}
