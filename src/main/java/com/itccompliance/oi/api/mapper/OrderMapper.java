package com.itccompliance.oi.api.mapper;

import com.itccompliance.oi.api.dto.OrderItemResponse;
import com.itccompliance.oi.api.dto.OrderResponse;
import com.itccompliance.oi.domain.model.Order;
import com.itccompliance.oi.domain.model.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public OrderResponse toOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(this::toOrderItemResponse)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getCustomerEmail(),
                order.getStatus(),
                itemResponses
        );
    }

    private OrderItemResponse toOrderItemResponse(OrderItem item) {
        return new OrderItemResponse(item.getId(), item.getSku(), item.getQuantity());
    }
}