package com.cameraclick.cart.exception;

import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Bắt các lỗi phát sinh khi thao tác giỏ hàng (đặc biệt là lúc gọi sang Product Service
 * qua OpenFeign) và trả về mã lỗi/nội dung rõ ràng thay vì để lộ lỗi 500 chung chung.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /** Sản phẩm không tồn tại (Product Service trả về 404) khi thêm vào giỏ hàng. */
    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<ApiError> handleProductNotFound(FeignException.NotFound ex) {
        return build(HttpStatus.NOT_FOUND, "Sản phẩm không tồn tại hoặc đã bị gỡ khỏi cửa hàng");
    }

    /** Product Service không phản hồi được (đang khởi động, mất kết nối Eureka, v.v.). */
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ApiError> handleFeignError(FeignException ex) {
        return build(HttpStatus.SERVICE_UNAVAILABLE,
                "Không thể lấy thông tin sản phẩm lúc này, vui lòng thử lại sau ít phút");
    }

    @ExceptionHandler({IllegalArgumentException.class, NumberFormatException.class})
    public ResponseEntity<ApiError> handleBadRequest(RuntimeException ex) {
        return build(HttpStatus.BAD_REQUEST, "Dữ liệu gửi lên không hợp lệ: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneral(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Có lỗi xảy ra ở giỏ hàng: " + ex.getMessage());
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(
                ApiError.builder().time(LocalDateTime.now()).status(status.value()).message(message).build()
        );
    }
}
