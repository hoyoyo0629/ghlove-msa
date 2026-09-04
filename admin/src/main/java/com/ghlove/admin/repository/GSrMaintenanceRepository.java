package com.ghlove.admin.repository;

import com.ghlove.admin.domain.GSrMaintenance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GSrMaintenanceRepository extends JpaRepository<GSrMaintenance, Long> {
    List<GSrMaintenance> findAllByOrderByBbsIdDesc();
}
