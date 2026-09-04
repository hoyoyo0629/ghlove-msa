package com.ghlove.member.repository;

import com.ghlove.member.domain.UserDataDestructionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserDataDestructionLogRepository extends JpaRepository<UserDataDestructionLog, Long> {

    List<UserDataDestructionLog> findTop200ByOrderByDestructionIdDesc();
}
