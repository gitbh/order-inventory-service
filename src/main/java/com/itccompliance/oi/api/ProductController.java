package com.itccompliance.oi.api;

import com.itccompliance.oi.api.dto.CreateProductRequest;
import com.itccompliance.oi.api.dto.UpdateProductRequest;
import com.itccompliance.oi.api.mapper.ProductMapper;
import com.itccompliance.oi.domain.model.Product;
import com.itccompliance.oi.domain.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;
    private final ProductMapper productMapper;

    public ProductController(ProductService productService, ProductMapper productMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
    }

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody @Valid CreateProductRequest request) {
        Product product = productMapper.mapToProduct(request);
        Product createdProduct = productService.create(product);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header("Location", "/products/" + createdProduct.getSku())
                .body(createdProduct);
    }

    @GetMapping("/{sku}")
    public ResponseEntity<Product> getBySku(@PathVariable String sku) {
        Product product = productService.getBySku(sku);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<Product>> getLowStockProducts(
            @RequestParam @Min(value = 0) int threshold) {
        List<Product> lowStockProducts = productService.findProductsBelowStockThreshold(threshold);
        return ResponseEntity.ok(lowStockProducts);
    }

    @PatchMapping("/{sku}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable String sku,
            @RequestBody @Valid UpdateProductRequest updateRequest) {
        Product updatedProduct = productService.updateProduct(sku, updateRequest);
        return ResponseEntity.ok(updatedProduct);
    }
}