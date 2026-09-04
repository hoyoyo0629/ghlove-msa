package com.ghlove.admin.repository;

import com.ghlove.admin.domain.Seo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeoRepository extends JpaRepository<Seo, Integer> {
    List<Seo> findAllByOrderBySeoIdDesc();
}
