package com.ghlove.member.repository;

import com.ghlove.member.domain.UserChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserChangeLogRepository extends JpaRepository<UserChangeLog, Long> {
}
