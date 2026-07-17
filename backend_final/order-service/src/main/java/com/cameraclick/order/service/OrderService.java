package com.cameraclick.order.service;

import com.cameraclick.order.client.ProductServiceClient;
import com.cameraclick.order.client.PaymentServiceClient;
import com.cameraclick.order.client.UserServiceClient;
import com.cameraclick.order.dto.*;
import com.cameraclick.order.entity.Order;
import com.cameraclick.order.entity.OrderDetail;
import com.cameraclick.order.entity.OrderStatus;
import com.cameraclick.order.event.OrderCreatedEvent;
import com.cameraclick.order.exception.BadRequestException;
import com.cameraclick.order.exception.ResourceNotFoundException;
import com.cameraclick.order.exception.ServiceUnavailableException;
import com.cameraclick.order.exception.UnauthorizedException;
import com.cameraclick.order.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;
    private final UserServiceClient userServiceClient;
    private final PaymentServiceClient paymentServiceClient;
    private final OrderEventProducer orderEventProducer;

    /**
     * Business flow (mục 19 trong tài liệu):
     * 1) Kiểm tra tồn kho từng sản phẩm qua Product Service (OpenFeign)
     * 2) Lấy thông tin người mua qua User Service (OpenFeign)
     * 3) Lưu đơn hàng vào MySQL
     * 4) Gọi Payment Service để xử lý thanh toán (OpenFeign)
     * 5) Trừ tồn kho ở Product Service
     * 6) Phát sự kiện OrderCreated lên Kafka -> Notification Service gửi email
     */
    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        UserInfo buyer = fetchUser(userId);

        List<OrderDetail> details = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest item : request.getItems()) {
            StockCheckResponse stock = checkStock(item.getProductId(), item.getQuantity());
            if (!stock.isAvailable()) {
                throw new BadRequestException("Sản phẩm '" + stock.getName() + "' không đủ số lượng tồn kho");
            }
            BigDecimal lineTotal = stock.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(lineTotal);

            details.add(OrderDetail.builder()
                    .productId(item.getProductId())
                    .productName(stock.getName())
                    .color(item.getColor())
                    .price(stock.getPrice())
                    .quantity(item.getQuantity())
                    .build());
        }

        Order order = Order.builder()
                .userId(userId)
                .totalAmount(total)
                .status(OrderStatus.PENDING)
                .shippingAddress(request.getShippingAddress())
                .receiverName(request.getReceiverName())
                .receiverPhone(request.getReceiverPhone())
                .paymentMethod(request.getPaymentMethod())
                .build();

        for (OrderDetail d : details) {
            d.setOrder(order);
        }
        order.setOrderDetails(details);
        order = orderRepository.save(order);

        // Process payment synchronously (OpenFeign)
        PaymentResponse payment = processPayment(order, request.getPaymentMethod());
        order.setStatus("SUCCESS".equalsIgnoreCase(payment.getStatus()) ? OrderStatus.CONFIRMED : OrderStatus.PENDING);
        order.setUpdatedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        // Deduct stock at Product Service
        for (OrderDetail detail : order.getOrderDetails()) {
            productServiceClient.updateStock(new StockUpdateRequest(detail.getProductId(), -detail.getQuantity()));
        }

        // Publish async event to Kafka -> Notification Service
        orderEventProducer.publishOrderCreated(OrderCreatedEvent.builder()
                .orderId(order.getId())
                .userId(userId)
                .userEmail(buyer.getEmail())
                .receiverName(order.getReceiverName())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .build());

        return toResponse(order);
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "checkStockFallback")
    public StockCheckResponse checkStock(Long productId, int quantity) {
        return productServiceClient.checkStock(productId, quantity);
    }

    public StockCheckResponse checkStockFallback(Long productId, int quantity, Throwable t) {
        log.error("Product Service unavailable while checking stock for product {}: {}", productId, t.getMessage());
        throw new ServiceUnavailableException("Dịch vụ sản phẩm hiện không khả dụng, vui lòng thử lại sau");
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "fetchUserFallback")
    public UserInfo fetchUser(Long userId) {
        return userServiceClient.getUserById(userId);
    }

    public UserInfo fetchUserFallback(Long userId, Throwable t) {
        log.error("User Service unavailable while fetching user {}: {}", userId, t.getMessage());
        throw new ServiceUnavailableException("Dịch vụ người dùng hiện không khả dụng, vui lòng thử lại sau");
    }

    @CircuitBreaker(name = "paymentService", fallbackMethod = "processPaymentFallback")
    public PaymentResponse processPayment(Order order, String method) {
        return paymentServiceClient.processPayment(PaymentRequest.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .amount(order.getTotalAmount())
                .method(method)
                .build());
    }

    public PaymentResponse processPaymentFallback(Order order, String method, Throwable t) {
        log.error("Payment Service unavailable for order {}: {}", order.getId(), t.getMessage());
        return PaymentResponse.builder().orderId(order.getId()).status("PENDING").build();
    }

    public List<OrderResponse> getOrdersByUser(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::toResponse).toList();
    }

    public OrderResponse getOrderById(Long id, Long userId, String role) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        if (!order.getUserId().equals(userId) && !"ADMIN".equalsIgnoreCase(role)) {
            throw new UnauthorizedException("Bạn không có quyền xem đơn hàng này");
        }
        return toResponse(order);
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public OrderResponse updateStatus(Long id, String statusStr) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Trạng thái không hợp lệ: " + statusStr);
        }
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse cancelOrder(Long id, Long userId) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        if (!order.getUserId().equals(userId)) {
            throw new UnauthorizedException("Bạn không có quyền hủy đơn hàng này");
        }
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.SHIPPING) {
            throw new BadRequestException("Không thể hủy đơn hàng đang giao hoặc đã hoàn thành");
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());

        // Restock products
        for (OrderDetail detail : order.getOrderDetails()) {
            productServiceClient.updateStock(new StockUpdateRequest(detail.getProductId(), detail.getQuantity()));
        }

        return toResponse(orderRepository.save(order));
    }

    private OrderResponse toResponse(Order order) {
        List<OrderDetailResponse> items = order.getOrderDetails().stream()
                .map(d -> OrderDetailResponse.builder()
                        .productId(d.getProductId())
                        .productName(d.getProductName())
                        .color(d.getColor())
                        .price(d.getPrice())
                        .quantity(d.getQuantity())
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .shippingAddress(order.getShippingAddress())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .paymentMethod(order.getPaymentMethod())
                .items(items)
                .createdAt(order.getCreatedAt())
                .build();
    }
}
