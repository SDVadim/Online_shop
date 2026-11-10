package org.example.repository;

import org.example.repository.Entities.ProductEntityLocal;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

//@Repository
public class LocalRepository /* implements ProductsRepository */{

  private List<ProductEntityLocal> products = new ArrayList<>();
  AtomicLong counter = new AtomicLong(0);

//  @Override
  public List<ProductEntityLocal> findAll() {
    return products;
  }

//  @Override
  public ProductEntityLocal findById(Long id) {
    return products.stream()
        .filter(product -> product.getId().equals(id))
        .findFirst()
        .orElse(null);
  }

//  @Override
  public Long addProduct(String name, BigDecimal price) {
    ProductEntityLocal entity = new ProductEntityLocal(name, price);
    entity.setId(counter.incrementAndGet());
    products.add(entity);
    return entity.getId();
  }

//  @Override
  public void updateProduct(ProductEntityLocal product) {
    for (ProductEntityLocal productEntity : products) {
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
