package com.ghlove.donation.repository;

import com.ghlove.donation.domain.DesignatedNotice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DesignatedNoticeRepository extends JpaRepository<DesignatedNotice, Long> {

    /** AS-IS: ORDER BY DSGN_DNTN_BIZ_NTC_ID DESC */
    List<DesignatedNotice> findByDsgnDntnBizIdOrderByNoticeIdDesc(Long dsgnDntnBizId);

    /** 대민(FRONT)은 AS-IS 매퍼가 RLS_YN='Y' 를 건다 - 비공개 공지는 안 보인다. */
    List<DesignatedNotice> findByDsgnDntnBizIdAndRlsYnOrderByNoticeIdDesc(Long dsgnDntnBizId, String rlsYn);
}
