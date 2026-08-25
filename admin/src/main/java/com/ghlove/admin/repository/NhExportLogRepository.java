package com.ghlove.admin.repository;

import com.ghlove.admin.domain.NhExportLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NhExportLogRepository extends JpaRepository<NhExportLog, String> {
}
