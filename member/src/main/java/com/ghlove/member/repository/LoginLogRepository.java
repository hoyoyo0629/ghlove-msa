package com.ghlove.member.repository;

import com.ghlove.member.domain.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoginLogRepository extends JpaRepository<LoginLog, Integer> {

    List<LoginLog> findTop200ByOrderByLoginLogIdDesc();
}
