package com.cameraclick.cart.service;

import com.cameraclick.cart.client.ProductServiceClient;
import com.cameraclick.cart.dto.AddToCartRequest;
import com.cameraclick.cart.dto.ProductInfo;
import com.cameraclick.cart.dto.CartResponse;
import com.cameraclick.cart.dto.UpdateCartRequest;
import com.cameraclick.cart.model.CartItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/**
 * Cart is stored entirely in Redis: key = "cart:{userId}", each field = productId, value = CartItem (JSON).
 * No relational database is used for the cart, per the microservices design (Cart Service -> Redis only).
 */
@Service

public class CartService {

    private final HashOperations<String, String, Object> hashOperations;
private final ProductServiceClient productServiceClient;

public CartService(RedisTemplate<String, Object> redisTemplate,
                   ProductServiceClient productServiceClient) {
    this.hashOperations = redisTemplate.opsForHash();
    this.productServiceClient = productServiceClient;
}

    private String key(Long userId) {
        return "cart:" + userId;
    }

    public CartResponse getCart(Long userId) {
        Collection<Object> values = hashOperations.values(key(userId));
        List<CartItem> items = values.stream()
                .filter(CartItem.class::isInstance)
                .map(CartItem.class::cast)
                .toList();
        return buildResponse(items);
    }

    public CartResponse addToCart(Long userId, AddToCartRequest request) {
        if (request.getProductId() == null) {
            throw new IllegalArgumentException("Thiếu productId");
        }
        ProductInfo product = productServiceClient.getProductById(request.getProductId());
        String field = String.valueOf(request.getProductId());

        Object rawExisting = hashOperations.get(key(userId), field);
        CartItem existing = rawExisting instanceof CartItem ? (CartItem) rawExisting : null;
        int newQuantity = request.getQuantity() != null ? request.getQuantity() : 1;
        if (existing != null) {
            newQuantity += existing.getQuantity();
        }

        CartItem item = CartItem.builder()
        .productId(product.getId())
        .name(product.getName())
        .price(product.getPrice())
        .quantity(newQuantity)
        .imageUrl(product.getImageUrl())
        .color(request.getColor())
        .build();

        hashOperations.put(key(userId), field, item);
        return getCart(userId);
    }

    public CartResponse updateQuantity(Long userId, UpdateCartRequest request) {
        String field = String.valueOf(request.getProductId());
        Object raw = hashOperations.get(key(userId), field);
        CartItem existing = raw instanceof CartItem ? (CartItem) raw : null;
        if (existing == null) {
            return getCart(userId);
        }
        if (request.getQuantity() <= 0) {
            hashOperations.delete(key(userId), field);
        } else {
            existing.setQuantity(request.getQuantity());
            hashOperations.put(key(userId), field, existing);
        }
        return getCart(userId);
    }

    public CartResponse removeItem(Long userId, Long productId) {
        hashOperations.delete(key(userId), String.valueOf(productId));
        return getCart(userId);
    }

    public void clearCart(Long userId) {
        hashOperations.getOperations().delete(key(userId));
    }

    private CartResponse buildResponse(List<CartItem> items) {
        BigDecimal total = items.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalItems = items.stream().mapToInt(CartItem::getQuantity).sum();
        return CartResponse.builder().items(items).totalAmount(total).totalItems(totalItems).build();
    }
}
