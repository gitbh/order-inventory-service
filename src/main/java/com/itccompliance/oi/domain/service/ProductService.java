package com.itccompliance.oi.domain.service;

import com.itccompliance.oi.api.dto.UpdateProductRequest;
import com.itccompliance.oi.api.mapper.ProductMapper;
import com.itccompliance.oi.domain.exception.ProductNotFoundException;
import com.itccompliance.oi.domain.model.Product;
import com.itccompliance.oi.persistence.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Transactional
    public Product create(Product product) {
        productRepository.findBySku(product.getSku())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Product with SKU '" + product.getSku() + "' already exists"
                    );
                });

        return productRepository.save(product);
    }

    public Product getBySku(String sku) {
        return productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException(sku));
    }

    public List<Product> findProductsBelowStockThreshold(int threshold) {
        return productRepository.findByAvailableQuantityLessThanEqual(threshold);
    }

    @Transactional
    public Product updateProduct(String sku, UpdateProductRequest updateRequest) {
        Product product = getBySku(sku);
        productMapper.applyUpdateToProduct(updateRequest, product);
        return productRepository.save(product);
    }
}