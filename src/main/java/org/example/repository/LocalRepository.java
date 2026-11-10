package org.example.repository;

import org.example.repository.Entities.ProductEntityExample;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

//@Repository
public class LocalRepository /* implements ProductsRepository */{

  private List<ProductEntityExample> products = new ArrayList<>();
  AtomicLong counter = new AtomicLong(0);

//  @Override
  public List<ProductEntityExample> findAll() {
    return products;
  }

//  @Override
  public ProductEntityExample findById(Long id) {
    return products.stream()
        .filter(product -> product.getId().equals(id))
        .findFirst()
        .orElse(null);
  }

//  @Override
  public Long addProduct(String name, BigDecimal price) {
    ProductEntityExample entity = new ProductEntityExample(name, price);
    entity.setId(counter.incrementAndGet());
    products.add(entity);
    return entity.getId();
  }

//  @Override
  public void updateProduct(ProductEntityExample product) {
    for (ProductEntityExample productEntity : products) {
      if (productEntity.getId().equals(product.getId())) {
        products.set(products.indexOf(productEntity),product);
      }
    }
  }

//  @Override
  public void deleteProduct(Long id) {
      products.removeIf(product -> product.getId().equals(id));
  }
}
