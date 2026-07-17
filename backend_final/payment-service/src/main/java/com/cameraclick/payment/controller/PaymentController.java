package com.cameraclick.payment.controller;

import com.cameraclick.payment.dto.PaymentRequest;
import com.cameraclick.payment.dto.PaymentResponse;
import com.cameraclick.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.processPayment(request));
    }

    @GetMapping("/status")
    public ResponseEntity<PaymentResponse> getStatus(@RequestParam Long orderId) {
        return ResponseEntity.ok(paymentService.getStatusByOrderId(orderId));
    }
}
