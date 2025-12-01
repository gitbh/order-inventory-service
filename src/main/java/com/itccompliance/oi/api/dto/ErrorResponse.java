package com.itccompliance.oi.api.dto;

import java.time.LocalDateTime;

public record ErrorResponse (LocalDateTime timestamp, String error, String message) {

}
