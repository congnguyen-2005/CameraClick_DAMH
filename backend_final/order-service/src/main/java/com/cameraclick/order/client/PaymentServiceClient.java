package com.cameraclick.order.client;

import com.cameraclick.order.dto.PaymentRequest;
import com.cameraclick.order.dto.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/** Synchronous (OpenFeign) communication: Order Service -> Payment Service. */
@FeignClient(name = "payment-service")
public interface PaymentServiceClient {

    @PostMapping("/api/payment")
    PaymentResponse processPayment(@RequestBody PaymentRequest request);
}
