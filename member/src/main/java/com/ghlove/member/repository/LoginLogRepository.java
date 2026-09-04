package com.ghlove.member.repository;

import com.ghlove.member.domain.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoginLogRepository extends JpaRepository<LoginLog, Integer> {

    List<LoginLog> findTop200ByOrderByLoginLogIdDesc();

    /** SFR-002 "로그 데이터... 익명화" - 보관기간이 지났고 아직 익명화되지 않은 로그(REMOTE_ADDR로 판별). */
    List<LoginLog> findByLoginDateBeforeAndRemoteAddrNot(String cutoff, String anonymizedMarker);
}
