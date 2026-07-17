package com.cameraclick.banner.repository;

import com.cameraclick.banner.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {
    List<Banner> findAllByOrderByDisplayOrderAsc();

    List<Banner> findByActiveTrueOrderByDisplayOrderAsc();
}
