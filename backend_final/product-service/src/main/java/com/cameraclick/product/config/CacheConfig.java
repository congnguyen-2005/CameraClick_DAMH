package com.cameraclick.product.config;

/**
 * Đã tắt cache Redis cho product-service.
 *
 * Trước đây ProductService dùng @Cacheable/@CacheEvict để cache danh sách/chi tiết
 * sản phẩm vào Redis. Việc này liên tục gây lỗi serialize/deserialize (do đổi qua
 * lại giữa JDK serialization và JSON serialization khiến dữ liệu cache cũ không
 * tương thích với cấu hình mới). Vì cache chỉ tăng tốc độ đọc chứ không phải yêu
 * cầu bắt buộc, ta bỏ hẳn để hệ thống chạy ổn định; có thể bật lại sau khi build
 * kỹ càng và luôn FLUSHALL Redis mỗi khi đổi cấu hình serializer.
 */
public class CacheConfig {
}
