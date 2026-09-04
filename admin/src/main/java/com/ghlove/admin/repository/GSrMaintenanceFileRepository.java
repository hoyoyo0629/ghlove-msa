package com.ghlove.admin.repository;

import com.ghlove.admin.domain.GSrMaintenanceFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GSrMaintenanceFileRepository extends JpaRepository<GSrMaintenanceFile, Long> {
    List<GSrMaintenanceFile> findByBbsIdOrderByAtchFileSeq(Long bbsId);
}
