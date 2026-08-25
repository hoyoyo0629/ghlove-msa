package com.ghlove.admin.repository;

import com.ghlove.admin.domain.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoginLogRepository extends JpaRepository<LoginLog, Integer> {

    List<LoginLog> findTop200ByOrderByLoginLogIdDesc();
}
