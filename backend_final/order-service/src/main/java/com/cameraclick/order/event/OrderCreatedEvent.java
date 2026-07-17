package com.cameraclick.order.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/** Published to Kafka topic "order-events" after an order is successfully created (async notification flow). */
@Data
@Builder
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
