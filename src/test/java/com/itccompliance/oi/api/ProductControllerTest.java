package com.itccompliance.oi.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itccompliance.oi.api.dto.CreateProductRequest;
import com.itccompliance.oi.api.dto.UpdateProductRequest;
import com.itccompliance.oi.api.mapper.ProductMapper;
import com.itccompliance.oi.domain.exception.ProductNotFoundException;
import com.itccompliance.oi.domain.model.Product;
import com.itccompliance.oi.domain.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductService productService;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductController productController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private Product createTestProduct() {
        Product p = new Product();
        p.setId(1L);
        p.setSku("TEST-SKU");
        p.setName("Test Product");
        p.setPrice(new BigDecimal("99.99"));
        p.setAvailableQuantity(50);
        return p;
    }

    @Test
    void createProduct_Success() throws Exception {
        Product product = createTestProduct();
        CreateProductRequest request = new CreateProductRequest(
                "TEST-SKU", "Test Product", new BigDecimal("99.99"), 50);

        when(productMapper.mapToProduct(request)).thenReturn(product);
        when(productService.create(product)).thenReturn(product);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/products/TEST-SKU"))
                .andExpect(jsonPath("$.sku").value("TEST-SKU"));

        verify(productMapper).mapToProduct(request);
        verify(productService).create(product);
    }

    @Test
    void createProduct_InvalidRequest_Returns400() throws Exception {
        CreateProductRequest request = new CreateProductRequest(
                "", "Test", new BigDecimal("0.00"), -1);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productMapper);
        verifyNoInteractions(productService);
    }

    @Test
    void getProduct_Success() throws Exception {
        Product product = createTestProduct();
        when(productService.getBySku("TEST-SKU")).thenReturn(product);

        mockMvc.perform(get("/products/TEST-SKU"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("TEST-SKU"));

        verify(productService).getBySku("TEST-SKU");
    }

    @Test
    void getProduct_NotFound_Returns404() throws Exception {
        when(productService.getBySku("NOTFOUND"))
                .thenThrow(new ProductNotFoundException("NOTFOUND"));

        mockMvc.perform(get("/products/NOTFOUND"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Product not found with SKU: NOTFOUND"));

        verify(productService).getBySku("NOTFOUND");
    }

    @Test
    void updateProduct_Success() throws Exception {
        Product product = createTestProduct();
        product.setName("Updated Name");
        product.setPrice(new BigDecimal("129.99"));

        UpdateProductRequest request = new UpdateProductRequest(
                "Updated Name", new BigDecimal("129.99"), null);

        when(productService.updateProduct("TEST-SKU", request)).thenReturn(product);

        mockMvc.perform(patch("/products/TEST-SKU")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.price").value(129.99));

        verify(productService).updateProduct("TEST-SKU", request);
    }

    @Test
    void updateProduct_NotFound_Returns404() throws Exception {
        UpdateProductRequest request = new UpdateProductRequest(
                "Updated Name", new BigDecimal("129.99"), null);

        when(productService.updateProduct("NOTFOUND", request))
                .thenThrow(new ProductNotFoundException("Product not found with SKU: NOTFOUND"));

        mockMvc.perform(patch("/products/NOTFOUND")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));

        verify(productService).updateProduct("NOTFOUND", request);
    }

    @Test
    void getLowStockProducts_Success() throws Exception {
        Product product = createTestProduct();
        when(productService.findProductsBelowStockThreshold(10))
                .thenReturn(List.of(product));

        mockMvc.perform(get("/products/low-stock")
                        .param("threshold", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sku").value("TEST-SKU"))
                .andExpect(jsonPath("$[0].availableQuantity").value(50));

        verify(productService).findProductsBelowStockThreshold(10);
    }

    @Test
    void getLowStockProducts_InvalidThreshold_Returns400() throws Exception {
        mockMvc.perform(get("/products/low-stock")
                        .param("threshold", "-1"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }
}