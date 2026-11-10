package org.example.repository;

import org.example.repository.Entities.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ProductsRepository extends JpaRepository<ProductEntity, Long> {}
