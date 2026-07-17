package com.cameraclick.banner.controller;

import com.cameraclick.banner.dto.BannerRequest;
import com.cameraclick.banner.entity.Banner;
import com.cameraclick.banner.exception.UnauthorizedException;
import com.cameraclick.banner.service.BannerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService service;

    /**
     * Public - dùng cho trang chủ: chỉ trả về banner đang bật (active),
     * đã sắp xếp theo thứ tự hiển thị.
     */
    @GetMapping
    public ResponseEntity<List<Banner>> getActiveBanners() {
        return ResponseEntity.ok(service.getActiveBanners());
    }

    /**
     * Admin - dùng cho trang quản trị: trả về TẤT CẢ banner (kể cả đang ẩn)
     * để admin có thể thấy và chỉnh sửa.
     */
    @GetMapping("/admin")
    public ResponseEntity<List<Banner>> getAllForAdmin(@RequestHeader("X-User-Role") String role) {
        requireAdmin(role);
        return ResponseEntity.ok(service.getAllBanners());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Banner> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<Banner> create(@RequestHeader("X-User-Role") String role,
                                          @Valid @RequestBody BannerRequest request) {
        requireAdmin(role);
        return ResponseEntity.ok(service.createBanner(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Banner> update(@PathVariable Long id, @RequestHeader("X-User-Role") String role,
                                          @Valid @RequestBody BannerRequest request) {
        requireAdmin(role);
        return ResponseEntity.ok(service.updateBanner(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestHeader("X-User-Role") String role) {
        requireAdmin(role);
        service.deleteBanner(id);
        return ResponseEntity.noContent().build();
    }

    private void requireAdmin(String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new UnauthorizedException("Chỉ ADMIN mới có quyền thực hiện thao tác này");
        }
    }
}
