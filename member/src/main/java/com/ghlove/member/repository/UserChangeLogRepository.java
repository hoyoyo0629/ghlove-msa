package com.ghlove.member.repository;

import com.ghlove.member.domain.UserChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserChangeLogRepository extends JpaRepository<UserChangeLog, Long> {
    List<UserChangeLog> findTop200ByOrderByChangeLogIdDesc();
}
