package com.ghlove.gift.repository;

import com.ghlove.gift.domain.Featured;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeaturedRepository extends JpaRepository<Featured, Integer> {
    List<Featured> findByFeaturedTypeOrderByFeaturedIdDesc(String featuredType);
    List<Featured> findByFeaturedTypeAndFeaturedNameContainingOrderByFeaturedIdDesc(String featuredType, String featuredName);
}
