package com.cameraclick.cart.exception;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ApiError {
    private LocalDateTime time;
    private int status;
    private String message;
}
