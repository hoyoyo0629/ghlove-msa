package com.ghlove.admin.repository;

import com.ghlove.admin.domain.CmntyFaqBbs;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CmntyFaqBbsRepository extends JpaRepository<CmntyFaqBbs, Long> {
    List<CmntyFaqBbs> findAllByOrderByBbsIdDesc();
}
