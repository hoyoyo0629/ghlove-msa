package com.ghlove.donation.repository;

import com.ghlove.donation.domain.LocgovLmtt;
import com.ghlove.donation.domain.LocgovLmttId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocgovLmttRepository extends JpaRepository<LocgovLmtt, LocgovLmttId> {
    List<LocgovLmtt> findByLocgovCodeOrderByLmttBgnDeDesc(String locgovCode);

    /** AS-IS ngdonation-mapper.getCntrLmtt - 오늘이 제한기간 안에 드는 행을 찾는다
     *  (LMTT_BGN_DE &lt;= 오늘 &lt;= LMTT_END_DE). 날짜는 yyyyMMdd 문자열이라 사전식 비교가
     *  곧 날짜 비교다. 기간이 겹치는 행이 여럿이면 먼저 시작한 것을 쓴다. */
    List<LocgovLmtt> findByLocgovCodeAndLmttBgnDeLessThanEqualAndLmttEndDeGreaterThanEqualOrderByLmttBgnDeAsc(
            String locgovCode, String today, String sameToday);
}
