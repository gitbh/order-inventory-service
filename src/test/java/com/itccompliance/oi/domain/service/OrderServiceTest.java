package com.itccompliance.oi.domain.service;

import com.itccompliance.oi.api.dto.OrderItemRequest;
import com.itccompliance.oi.domain.exception.InsufficientStockException;
import com.itccompliance.oi.domain.exception.ProductNotFoundException;
import com.itccompliance.oi.domain.model.Order;
import com.itccompliance.oi.domain.model.OrderStatus;
import com.itccompliance.oi.domain.model.Product;
import com.itccompliance.oi.persistence.OrderRepository;
import com.itccompliance.oi.persistence.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
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
class OrderServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private FulfilmentService fulfilmentService;

    @InjectMocks
    private OrderService orderService;

    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        product1 = new Product();
        product1.setId(1L);
        product1.setSku("SKU001");
        product1.setName("Product 1");
        product1.setPrice(new BigDecimal("19.99"));
        product1.setAvailableQuantity(10);

        product2 = new Product();
        product2.setId(2L);
        product2.setSku("SKU002");
        product2.setName("Product 2");
        product2.setPrice(new BigDecimal("29.99"));
        product2.setAvailableQuantity(5);
    }

    @Test
    void createOrder_ShouldReserveStockAndCreateOrder_WhenSufficientStock() {
        String customerEmail = "test@example.com";
        List<OrderItemRequest> items = List.of(
                new OrderItemRequest("SKU001", 3),
                new OrderItemRequest("SKU002", 2)
        );

        when(productRepository.findBySku("SKU001")).thenReturn(Optional.of(product1));
        when(productRepository.findBySku("SKU002")).thenReturn(Optional.of(product2));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(100L);
            return order;
        });

        Order order = orderService.createOrder(customerEmail, items);

        assertThat(order).isNotNull();
        assertThat(order.getId()).isEqualTo(100L);
        assertThat(order.getCustomerEmail()).isEqualTo(customerEmail);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.RESERVED);
        assertThat(order.getItems()).hasSize(2);

        assertThat(product1.getAvailableQuantity()).isEqualTo(7);
        assertThat(product2.getAvailableQuantity()).isEqualTo(3);

        verify(productRepository, times(2)).save(any(Product.class));

        verify(fulfilmentService).processFulfilment(100L);
    }

    @Test
    void createOrder_ShouldThrowInsufficientStockException_WhenStockInsufficient() {
        String customerEmail = "test@example.com";
        List<OrderItemRequest> items = List.of(
                new OrderItemRequest("SKU001", 15)
        );

        when(productRepository.findBySku("SKU001")).thenReturn(Optional.of(product1));

        assertThatThrownBy(() -> orderService.createOrder(customerEmail, items))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("SKU001");

        verify(productRepository, never()).save(any(Product.class));
        verify(orderRepository, never()).save(any(Order.class));
        verify(fulfilmentService, never()).processFulfilment(any());
    }

    @Test
    void createOrder_ShouldThrowProductNotFoundException_WhenProductNotFound() {
        String customerEmail = "test@example.com";
        List<OrderItemRequest> items = List.of(
                new OrderItemRequest("NONEXISTENT", 1)
        );

        when(productRepository.findBySku("NONEXISTENT")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(customerEmail, items))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("NONEXISTENT");

        verify(productRepository, never()).save(any(Product.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_ShouldHandleMultipleItemsCorrectly() {
        String customerEmail = "test@example.com";
        List<OrderItemRequest> items = List.of(
                new OrderItemRequest("SKU001", 1),
                new OrderItemRequest("SKU001", 2)
        );

        when(productRepository.findBySku("SKU001")).thenReturn(Optional.of(product1));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(100L);
            return order;
        });

        Order order = orderService.createOrder(customerEmail, items);

        assertThat(order).isNotNull();
        assertThat(order.getItems()).hasSize(2);

        assertThat(product1.getAvailableQuantity()).isEqualTo(7);
    }

    @Test
    void getOrder_ShouldReturnOrder_WhenExists() {
        Long orderId = 100L;
        Order order = new Order();
        order.setId(orderId);
        order.setCustomerEmail("test@example.com");
        order.setStatus(OrderStatus.RESERVED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Order result = orderService.getOrder(orderId);

        assertThat(result).isEqualTo(order);
        verify(orderRepository).findById(orderId);
    }
}