package com.ghlove.member.repository;

import com.ghlove.member.domain.MberSecsn;
import com.ghlove.member.domain.MberSecsnId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** 탈퇴회원 연간 기부액 스냅샷 (AS-IS `G_MBER_SECSN`). */
public interface MberSecsnRepository extends JpaRepository<MberSecsn, MberSecsnId> {

    /**
     * AS-IS `ngdonation-mapper.xml`의 `getGMberSecsnSumCntrAmt` 재현 -
     * `select IFNULL(sum(cntr_amt),0) from G_MBER_SECSN where mber_ci = ? and secsn_year = 올해`.
     * 값이 없으면 0을 돌려준다(AS-IS의 `IFNULL`과 동일).
     */
    @Query("select coalesce(sum(m.cntrAmt), 0) from MberSecsn m "
            + "where m.id.mberCi = :mberCi and m.id.secsnYear = :secsnYear")
    long sumCntrAmtByCiAndYear(String mberCi, String secsnYear);
}
