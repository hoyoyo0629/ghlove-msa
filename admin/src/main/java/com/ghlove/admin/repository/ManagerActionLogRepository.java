package com.ghlove.admin.repository;

import com.ghlove.admin.domain.ManagerActionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ManagerActionLogRepository extends JpaRepository<ManagerActionLog, Integer> {

    List<ManagerActionLog> findTop200ByOrderByActionLogIdDesc();
}
