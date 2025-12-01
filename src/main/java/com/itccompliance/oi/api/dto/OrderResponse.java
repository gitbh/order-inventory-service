package com.itccompliance.oi.api.dto;

import com.itccompliance.oi.domain.model.OrderStatus;

import java.util.List;

public record OrderResponse(
        Long id,
        String customerEmail,
        OrderStatus status,
        List<OrderItemResponse> items
) {}