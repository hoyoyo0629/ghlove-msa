package com.ghlove.admin.repository;

import com.ghlove.admin.domain.CmntyFaqBbsCmnt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** 담당자용 FAQ 댓글 (AS-IS {@code G_CMNTY_FAQ_BBS_CMNT}). */
public interface CmntyFaqBbsCmntRepository extends JpaRepository<CmntyFaqBbsCmnt, Long> {

    Optional<CmntyFaqBbsCmnt> findByCmntIdAndUseYn(Long cmntId, String useYn);

    List<CmntyFaqBbsCmnt> findByBbsIdAndUseYn(Long bbsId, String useYn);
}
