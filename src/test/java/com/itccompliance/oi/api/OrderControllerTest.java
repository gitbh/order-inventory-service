package com.itccompliance.oi.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itccompliance.oi.api.dto.CreateOrderRequest;
import com.itccompliance.oi.api.dto.OrderItemRequest;
import com.itccompliance.oi.api.dto.OrderResponse;
import com.itccompliance.oi.api.mapper.OrderMapper;
import com.itccompliance.oi.domain.exception.InsufficientStockException;
import com.itccompliance.oi.domain.exception.OrderNotFoundException;
import com.itccompliance.oi.domain.model.Order;
import com.itccompliance.oi.domain.model.OrderItem;
import com.itccompliance.oi.domain.model.OrderStatus;
import com.itccompliance.oi.domain.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderController orderController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private Order createTestOrder() {
        Order order = new Order();
        order.setId(100L);
        order.setCustomerEmail("test@example.com");
        order.setStatus(OrderStatus.RESERVED);

        OrderItem item = new OrderItem();
        item.setSku("SKU001");
        item.setQuantity(2);
        item.setOrder(order);
        order.setItems(List.of(item));

        return order;
    }

    @Test
    void createOrder_Success() throws Exception {
        Order order = createTestOrder();
        OrderResponse response = new OrderResponse(100L, "test@example.com",
                OrderStatus.RESERVED, List.of());

        when(orderService.createOrder(any(), any())).thenReturn(order);
        when(orderMapper.toOrderResponse(order)).thenReturn(response);

        CreateOrderRequest request = new CreateOrderRequest(
                "test@example.com",
                List.of(new OrderItemRequest("SKU001", 2))
        );

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/orders/100"))
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void createOrder_InvalidRequest_Returns400() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(
                "invalid-email",
                List.of()
        );

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrder_InsufficientStock_Returns409() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(
                "test@example.com",
                List.of(new OrderItemRequest("SKU001", 100))
        );

        when(orderService.createOrder(eq("test@example.com"), any()))
                .thenThrow(new InsufficientStockException("SKU001"));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Insufficient stock for: SKU001"));
    }

    @Test
    void getOrder_Success() throws Exception {
        Order order = createTestOrder();
        OrderResponse response = new OrderResponse(100L, "test@example.com",
                OrderStatus.RESERVED, List.of());

        when(orderService.getOrder(100L)).thenReturn(order);
        when(orderMapper.toOrderResponse(order)).thenReturn(response);

        mockMvc.perform(get("/orders/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void getOrder_NotFound_Returns404() throws Exception {
        when(orderService.getOrder(999L))
                .thenThrow(new OrderNotFoundException(999L));

        mockMvc.perform(get("/orders/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Order not found with ID: 999"));
    }

    @Test
    void getOrders_ByStatus() throws Exception {
        Order order = createTestOrder();
        OrderResponse response = new OrderResponse(100L, "test@example.com",
                OrderStatus.RESERVED, List.of());

        when(orderService.getOrdersByStatus(OrderStatus.RESERVED))
                .thenReturn(List.of(order));
        when(orderMapper.toOrderResponse(order)).thenReturn(response);

        mockMvc.perform(get("/orders")
                        .param("status", "RESERVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("RESERVED"));
    }
}