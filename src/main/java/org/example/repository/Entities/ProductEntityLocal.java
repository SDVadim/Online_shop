package org.example.repository.Entities;

import java.math.BigDecimal;

public class ProductEntityLocal {
  private Long id;
  private String name;
  private BigDecimal price;

  public ProductEntityLocal(String name, BigDecimal price) {
    this.name = name;
    extracted(price);
  }

  private void extracted(BigDecimal price) {
    this.price = price;
  }

  public ProductEntityLocal(String name, BigDecimal price, Long id) {
    this.name = name;
    this.price = price;
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getId() {
    return id;
  }
}
