package com.itccompliance.oi.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(
        @Email String customerEmail,
        @NotEmpty List<@Valid OrderItemRequest> items
) {}
