package com.itccompliance.oi.domain.service;

import com.itccompliance.oi.api.dto.UpdateProductRequest;
import com.itccompliance.oi.api.mapper.ProductMapper;
import com.itccompliance.oi.domain.exception.ProductNotFoundException;
import com.itccompliance.oi.domain.model.Product;
import com.itccompliance.oi.persistence.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    @Test
    void create_Success() {
        Product product = createProduct("SKU001", 10);
        when(productRepository.findBySku("SKU001")).thenReturn(Optional.empty());
        when(productRepository.save(product)).thenReturn(product);

        Product result = productService.create(product);

        assertThat(result).isEqualTo(product);
        verify(productRepository).save(product);
    }

    @Test
    void create_ThrowsWhenSkuExists() {
        Product existingProduct = createProduct("SKU001", 10);
        when(productRepository.findBySku("SKU001")).thenReturn(Optional.of(existingProduct));

        assertThatThrownBy(() -> productService.create(createProduct("SKU001", 5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SKU001");

        verify(productRepository, never()).save(any());
    }

    @Test
    void getBySku_Success() {
        Product product = createProduct("SKU001", 10);
        when(productRepository.findBySku("SKU001")).thenReturn(Optional.of(product));

        Product result = productService.getBySku("SKU001");

        assertThat(result).isEqualTo(product);
    }

    @Test
    void getBySku_ThrowsWhenNotFound() {
        when(productRepository.findBySku("NOTFOUND")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getBySku("NOTFOUND"))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void findProductsBelowStockThreshold_Success() {
        Product lowStock = createProduct("LOW", 5);

        when(productRepository.findByAvailableQuantityLessThanEqual(10))
                .thenReturn(List.of(lowStock));

        List<Product> result = productService.findProductsBelowStockThreshold(10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSku()).isEqualTo("LOW");
    }

    @Test
    void findProductsBelowStockThreshold_ReturnsEmpty() {
        when(productRepository.findByAvailableQuantityLessThanEqual(5))
                .thenReturn(List.of());

        List<Product> result = productService.findProductsBelowStockThreshold(5);

        assertThat(result).isEmpty();
    }

    @Test
    void updateProduct_Success() {
        Product product = createProduct("SKU001", 10);
        UpdateProductRequest request = new UpdateProductRequest("New Name", new BigDecimal("199.99"), 20);

        when(productRepository.findBySku("SKU001")).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        Product result = productService.updateProduct("SKU001", request);

        assertThat(result).isEqualTo(product);
        verify(productMapper).applyUpdateToProduct(request, product);
        verify(productRepository).save(product);
    }

    @Test
    void updateProduct_ThrowsWhenNotFound() {
        UpdateProductRequest request = new UpdateProductRequest("New Name", null, null);
        when(productRepository.findBySku("NOTFOUND")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct("NOTFOUND", request))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productMapper, never()).applyUpdateToProduct(any(), any());
        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_PartialUpdate() {
        Product product = createProduct("SKU001", 10);
        UpdateProductRequest request = new UpdateProductRequest("Updated Name", null, null);

        when(productRepository.findBySku("SKU001")).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        productService.updateProduct("SKU001", request);

        verify(productMapper).applyUpdateToProduct(request, product);
        verify(productRepository).save(product);
    }

    private Product createProduct(String sku, int quantity) {
        Product product = new Product();
        product.setSku(sku);
        product.setName("Test Product " + sku);
        product.setPrice(new BigDecimal("99.99"));
        product.setAvailableQuantity(quantity);
        return product;
    }
}