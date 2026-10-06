package com.ghlove.admin.repository;

import com.ghlove.admin.domain.CmntyFaqBbsFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/** 담당자용 FAQ 본문 첨부파일 (AS-IS {@code G_CMNTY_FAQ_BBS_FILE}). */
public interface CmntyFaqBbsFileRepository extends JpaRepository<CmntyFaqBbsFile, Long> {

    List<CmntyFaqBbsFile> findByBbsIdAndUseYnOrderByAtchFileSeqAsc(Long bbsId, String useYn);

    @Query("select coalesce(max(f.atchFileSeq), 0) + 1 from CmntyFaqBbsFile f where f.bbsId = :bbsId")
    int nextSeq(Long bbsId);
}
