package com.ghlove.admin.repository;

import com.ghlove.admin.domain.CmntyOffSrBbsFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/** 오프라인 담당자 SR게시판 본문 첨부파일 (AS-IS {@code G_CMNTY_OFF_SR_BBS_FILE}). */
public interface CmntyOffSrBbsFileRepository extends JpaRepository<CmntyOffSrBbsFile, Long> {

    List<CmntyOffSrBbsFile> findByBbsIdAndUseYnOrderByAtchFileSeqAsc(Long bbsId, String useYn);

    @Query("select coalesce(max(f.atchFileSeq), 0) + 1 from CmntyOffSrBbsFile f where f.bbsId = :bbsId")
    int nextSeq(Long bbsId);
}
