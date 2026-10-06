package com.ghlove.admin.repository;

import com.ghlove.admin.domain.CmntyOffSrBbsCmnt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** 오프라인 담당자 SR게시판 댓글 (AS-IS {@code G_CMNTY_OFF_SR_BBS_CMNT}). */
public interface CmntyOffSrBbsCmntRepository extends JpaRepository<CmntyOffSrBbsCmnt, Long> {

    Optional<CmntyOffSrBbsCmnt> findByCmntIdAndUseYn(Long cmntId, String useYn);

    List<CmntyOffSrBbsCmnt> findByBbsIdAndUseYn(Long bbsId, String useYn);
}
