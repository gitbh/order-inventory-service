package com.itccompliance.oi.domain.service;

import com.itccompliance.oi.api.dto.OrderItemRequest;
import com.itccompliance.oi.domain.exception.InsufficientStockException;
import com.itccompliance.oi.domain.exception.OrderNotFoundException;
import com.itccompliance.oi.domain.exception.ProductNotFoundException;
import com.itccompliance.oi.domain.model.Order;
import com.itccompliance.oi.domain.model.OrderItem;
import com.itccompliance.oi.domain.model.OrderStatus;
import com.itccompliance.oi.domain.model.Product;
import com.itccompliance.oi.persistence.OrderRepository;
import com.itccompliance.oi.persistence.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final FulfilmentService fulfilmentService;

    public OrderService(ProductRepository productRepository, OrderRepository orderRepository, FulfilmentService fulfilmentService) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.fulfilmentService = fulfilmentService;
    }

    @Transactional
    public Order createOrder(String customerEmail, List<OrderItemRequest> orderItemRequests) {
        Order order = new Order();
        order.setCustomerEmail(customerEmail);

        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest orderItemRequest : orderItemRequests) {
            Product product = productRepository.findBySku(orderItemRequest.sku())
                    .orElseThrow(() ->
                            new ProductNotFoundException("Product not found: " + orderItemRequest.sku()));

            if (product.getAvailableQuantity() < orderItemRequest.quantity()) {
                throw new InsufficientStockException(product.getSku());
            }

            product.setAvailableQuantity(product.getAvailableQuantity() - orderItemRequest.quantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setSku(orderItemRequest.sku());
            orderItem.setQuantity(orderItemRequest.quantity());
            orderItem.setOrder(order);
            orderItems.add(orderItem);
        }

        order.setItems(orderItems);
        order.setStatus(OrderStatus.RESERVED);
        Order createdOrder = orderRepository.save(order);

        fulfilmentService.processFulfilment(createdOrder.getId());

        return createdOrder;
    }

    public Order getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}
