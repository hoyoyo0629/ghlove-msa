package com.ghlove.member.repository;

import com.ghlove.member.domain.UserActionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserActionLogRepository extends JpaRepository<UserActionLog, Integer> {

    List<UserActionLog> findTop200ByOrderByActionLogIdDesc();
}
