package com.itccompliance.oi.domain.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super("Insufficient stock for: " + message);
    }
}
