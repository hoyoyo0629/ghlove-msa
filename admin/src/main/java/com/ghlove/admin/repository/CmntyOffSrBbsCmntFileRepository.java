package com.ghlove.admin.repository;

import com.ghlove.admin.domain.CmntyOffSrBbsCmntFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

/** 오프라인 담당자 SR게시판 댓글 첨부파일 (AS-IS {@code G_CMNTY_OFF_SR_BBS_CMNT_FILE}). */
public interface CmntyOffSrBbsCmntFileRepository extends JpaRepository<CmntyOffSrBbsCmntFile, Long> {

    List<CmntyOffSrBbsCmntFile> findByCmntIdAndUseYn(Long cmntId, String useYn);

    List<CmntyOffSrBbsCmntFile> findByCmntIdInAndUseYn(Collection<Long> cmntIds, String useYn);

    @Query("select coalesce(max(f.atchFileSeq), 0) + 1 from CmntyOffSrBbsCmntFile f where f.cmntId = :cmntId")
    int nextSeq(Long cmntId);
}
