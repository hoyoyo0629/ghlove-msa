package com.ghlove.admin.repository;

import com.ghlove.admin.domain.CmntyBbs;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CmntyBbsRepository extends JpaRepository<CmntyBbs, Long> {
    List<CmntyBbs> findAllByOrderByBbsIdDesc();
}
