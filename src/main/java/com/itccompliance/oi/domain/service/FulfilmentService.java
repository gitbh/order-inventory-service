package com.itccompliance.oi.domain.service;

import com.itccompliance.oi.domain.model.Order;
import com.itccompliance.oi.domain.model.OrderStatus;
import com.itccompliance.oi.persistence.OrderRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class FulfilmentService {

    private final OrderRepository orderRepository;

    public FulfilmentService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Async
    @Transactional
    public void processFulfilment(Long orderId) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(100, 300));

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalStateException("Order not found: " + orderId));

            order.setStatus(OrderStatus.FULFILLED);
        } catch (InterruptedException e) {
            throw new RuntimeException("Fulfilment process interrupted for order: " + orderId, e);
        }
    }
}
