package com.ghlove.admin.repository;

import com.ghlove.admin.domain.CmntyOffSrBbs;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CmntyOffSrBbsRepository extends JpaRepository<CmntyOffSrBbs, Long> {
    List<CmntyOffSrBbs> findAllByOrderByBbsIdDesc();
}
