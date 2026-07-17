package com.cameraclick.notification.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/** Mirrors the event published by Order Service to the "order-events" Kafka topic. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent implements Serializable {
    private Long orderId;
    private Long userId;
    private String userEmail;
    private String receiverName;
    private BigDecimal totalAmount;
    private String status;
}
