package com.ghlove.gift.repository;

import com.ghlove.gift.domain.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BrandRepository extends JpaRepository<Brand, Integer> {
    List<Brand> findAllByOrderByBrandIdDesc();
}
