package com.ghlove.member.repository;

import com.ghlove.member.domain.UserLevelLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserLevelLogRepository extends JpaRepository<UserLevelLog, Long> {

    /** 등급 삭제 가드용 - 이 등급으로 로그가 남은 회원 목록(최신순, 회원당 최근 1건만 보고
     *  현재도 이 등급인지는 서비스단에서 판단). */
    List<UserLevelLog> findByLevelIdOrderByCreatedDateDesc(Integer levelId);

    List<UserLevelLog> findByUserIdOrderByCreatedDateDesc(Long userId);
}
