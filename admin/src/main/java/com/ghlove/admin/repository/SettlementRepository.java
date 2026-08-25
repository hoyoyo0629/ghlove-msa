package com.ghlove.admin.repository;

import com.ghlove.admin.domain.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    List<Settlement> findAllByOrderBySettlementIdDesc();
}
