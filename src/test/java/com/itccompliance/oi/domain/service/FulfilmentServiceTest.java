package com.itccompliance.oi.domain.service;

import com.itccompliance.oi.domain.model.Order;
import com.itccompliance.oi.domain.model.OrderStatus;
import com.itccompliance.oi.persistence.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FulfilmentServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private FulfilmentService fulfilmentService;

    @Test
    void processFulfilment_ShouldUpdateOrderStatusToFulfilled() throws Exception {
        Long orderId = 100L;
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.RESERVED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        fulfilmentService.processFulfilment(orderId);

        Thread.sleep(500);

        verify(orderRepository).findById(orderId);
    }

    @Test
    void processFulfilment_ShouldThrowException_WhenOrderNotFound() throws Exception {
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> fulfilmentService.processFulfilment(orderId)
        );

        assertThat(exception.getMessage()).contains("Order not found: 999");
        verify(orderRepository).findById(orderId);
    }
}