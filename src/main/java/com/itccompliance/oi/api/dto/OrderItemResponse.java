package com.itccompliance.oi.api.dto;

public record OrderItemResponse(
        Long id,
        String sku,
        int quantity
) {}