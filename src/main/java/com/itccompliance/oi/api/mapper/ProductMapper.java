package com.itccompliance.oi.api.mapper;

import com.itccompliance.oi.api.dto.CreateProductRequest;
import com.itccompliance.oi.api.dto.UpdateProductRequest;
import com.itccompliance.oi.domain.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product mapToProduct(CreateProductRequest request) {
        Product product = new Product();
        product.setSku(request.sku());
        product.setName(request.name());
        product.setPrice(request.price());
        product.setAvailableQuantity(request.availableQuantity());
        return product;
    }

    public void applyUpdateToProduct(UpdateProductRequest request, Product product) {
        if (request.name() != null) {
            product.setName(request.name());
        }
        if (request.price() != null) {
            product.setPrice(request.price());
        }
        if (request.availableQuantity() != null) {
            product.setAvailableQuantity(request.availableQuantity());
        }
    }
}