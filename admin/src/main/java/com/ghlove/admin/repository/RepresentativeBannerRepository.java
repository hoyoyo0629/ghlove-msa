package com.ghlove.admin.repository;

import com.ghlove.admin.domain.RepresentativeBanner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepresentativeBannerRepository extends JpaRepository<RepresentativeBanner, Integer> {
    List<RepresentativeBanner> findByProcessTypeOrderByDisplayOrderAsc(String processType);
}
