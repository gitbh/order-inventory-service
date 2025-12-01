package com.itccompliance.oi.async;

import com.itccompliance.oi.api.dto.CreateOrderRequest;
import com.itccompliance.oi.api.dto.CreateProductRequest;
import com.itccompliance.oi.api.dto.OrderItemRequest;
import com.itccompliance.oi.api.dto.UpdateProductRequest;
import com.itccompliance.oi.domain.model.Order;
import com.itccompliance.oi.domain.model.OrderStatus;
import com.itccompliance.oi.domain.model.Product;
import com.itccompliance.oi.persistence.OrderRepository;
import com.itccompliance.oi.persistence.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FulfilmentIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void shouldCreateProductViaApi() {
        CreateProductRequest request = new CreateProductRequest(
                "IT-SKU-001",
                "Integration Test Product",
                new BigDecimal("49.99"),
                100
        );

        ResponseEntity<Product> response = restTemplate.postForEntity(
                "/products",
                request,
                Product.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSku()).isEqualTo("IT-SKU-001");
    }


    @Test
    void shouldUpdateProductViaPatch() {
        UpdateProductRequest updateRequest = new UpdateProductRequest(
                "Updated Product Name",
                new BigDecimal("59.99"),
                null
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UpdateProductRequest> requestEntity = new HttpEntity<>(updateRequest, headers);

        ResponseEntity<Product> patchResponse = restTemplate.exchange(
                "/products/IT-SKU-001",
                HttpMethod.PATCH,
                requestEntity,
                Product.class
        );

        assertThat(patchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Product updatedProduct = patchResponse.getBody();
        assertThat(updatedProduct).isNotNull();
        assertThat(updatedProduct.getName()).isEqualTo("Updated Product Name");
        assertThat(updatedProduct.getPrice()).isEqualTo(new BigDecimal("59.99"));
        assertThat(updatedProduct.getAvailableQuantity()).isEqualTo(100);

        ResponseEntity<Product> verifyResponse = restTemplate.getForEntity(
                "/products/IT-SKU-001",
                Product.class
        );

        assertThat(verifyResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Product verifiedProduct = verifyResponse.getBody();
        assertThat(verifiedProduct).isNotNull();
        assertThat(verifiedProduct.getName()).isEqualTo("Updated Product Name");
        assertThat(verifiedProduct.getPrice()).isEqualTo(new BigDecimal("59.99"));
        assertThat(verifiedProduct.getAvailableQuantity()).isEqualTo(100);
    }

    @Test
    void orderShouldTransitionToFulfilled_AfterAsyncProcessing() throws InterruptedException {
        CreateOrderRequest request = new CreateOrderRequest(
                "integration.test@example.com",
                List.of(new OrderItemRequest("IT-SKU-001", 5))
        );

        ResponseEntity<Order> createResponse = restTemplate.postForEntity(
                "/orders",
                request,
                Order.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Order createdOrder = createResponse.getBody();
        assertThat(createdOrder).isNotNull();
        assertThat(createdOrder.getStatus()).isEqualTo(OrderStatus.RESERVED);

        Long orderId = createdOrder.getId();

        Thread.sleep(300);

        ResponseEntity<Order> getResponse = restTemplate.getForEntity(
                "/orders/" + orderId,
                Order.class
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().getStatus()).isEqualTo(OrderStatus.FULFILLED);

        Product updatedProduct = productRepository.findBySku("IT-SKU-001").orElseThrow();
        assertThat(updatedProduct.getAvailableQuantity()).isEqualTo(95);
    }

    @Test
    void shouldReturn409Conflict_WhenInsufficientStock() {
        CreateOrderRequest request = new CreateOrderRequest(
                "conflict.test@example.com",
                List.of(new OrderItemRequest("IT-SKU-001", 999))
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/orders",
                request,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).contains("Insufficient stock");

        Product product = productRepository.findBySku("IT-SKU-001").orElseThrow();
        assertThat(product.getAvailableQuantity()).isEqualTo(100);
    }

    @Test
    void shouldReturn400BadRequest_WhenInvalidRequestBody() {
        String invalidJson = """
        {
            "customerEmail": "not-an-email",
            "items": []
        }
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> requestEntity = new HttpEntity<>(invalidJson, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/orders",
                requestEntity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Bad Request");
    }
}