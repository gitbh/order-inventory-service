package com.itccompliance.oi.api;

import com.itccompliance.oi.api.dto.CreateOrderRequest;
import com.itccompliance.oi.api.dto.OrderResponse;
import com.itccompliance.oi.api.mapper.OrderMapper;
import com.itccompliance.oi.domain.model.Order;
import com.itccompliance.oi.domain.model.OrderStatus;
import com.itccompliance.oi.domain.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    public OrderController(OrderService orderService, OrderMapper orderMapper) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody @Valid CreateOrderRequest request) {
        Order order = orderService.createOrder(
                request.customerEmail(),
                request.items()
        );
        OrderResponse response =  orderMapper.toOrderResponse(order);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header("Location", "/orders/" + order.getId())
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        Order order = orderService.getOrder(id);
        OrderResponse response = orderMapper.toOrderResponse(order);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(
            @RequestParam(required = false) OrderStatus status) {

        List<Order> orders;
        if (status != null) {
            orders = orderService.getOrdersByStatus(status);
        } else {
            orders = orderService.getAllOrders();
        }

        List<OrderResponse> responses = orders.stream()
                .map(orderMapper::toOrderResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }
}
