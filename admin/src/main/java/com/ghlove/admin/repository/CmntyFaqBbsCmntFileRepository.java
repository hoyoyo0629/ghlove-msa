package com.ghlove.admin.repository;

import com.ghlove.admin.domain.CmntyFaqBbsCmntFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

/** 담당자용 FAQ 댓글 첨부파일 (AS-IS {@code G_CMNTY_FAQ_BBS_CMNT_FILE}). */
public interface CmntyFaqBbsCmntFileRepository extends JpaRepository<CmntyFaqBbsCmntFile, Long> {

    List<CmntyFaqBbsCmntFile> findByCmntIdAndUseYn(Long cmntId, String useYn);

    List<CmntyFaqBbsCmntFile> findByCmntIdInAndUseYn(Collection<Long> cmntIds, String useYn);

    @Query("select coalesce(max(f.atchFileSeq), 0) + 1 from CmntyFaqBbsCmntFile f where f.cmntId = :cmntId")
    int nextSeq(Long cmntId);
}
