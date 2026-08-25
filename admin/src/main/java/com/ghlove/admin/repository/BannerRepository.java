package com.ghlove.admin.repository;

import com.ghlove.admin.domain.Banner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Integer> {
    List<Banner> findAllByOrderByDisplayOrderAsc();

    List<Banner> findByDisplayFlagOrderByDisplayOrderAsc(String displayFlag);
}
