package org.example.Repository.Entities;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.UUID;

import java.math.BigDecimal;

public class ProductEntityExample {

  private Long id;
  private String name;
  private BigDecimal price;

  public ProductEntityExample(String name, BigDecimal price) {
    this.name = name;
    this.price = price;
  }

  public ProductEntityExample(String name, BigDecimal price, Long id) {
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
