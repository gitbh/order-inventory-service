package com.itccompliance.oi.persistence;

import com.itccompliance.oi.domain.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);
    List<Product> findByAvailableQuantityLessThanEqual(int threshold);
}

