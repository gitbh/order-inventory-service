package com.itccompliance.oi.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateProductRequest(
        String name,
        BigDecimal price,
        Integer availableQuantity
) {}