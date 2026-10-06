package com.ghlove.admin.repository;

import com.ghlove.admin.domain.CmntySrBbsFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/** SR게시판 본문 첨부파일 (AS-IS {@code G_CMNTY_SR_BBS_FILE}). */
public interface CmntySrBbsFileRepository extends JpaRepository<CmntySrBbsFile, Long> {

    /** AS-IS getSrBbsfileList - 살아있는 첨부만, 순번 순. */
    List<CmntySrBbsFile> findByBbsIdAndUseYnOrderByAtchFileSeqAsc(Long bbsId, String useYn);

    /** AS-IS insertSrBbsFile의 {@code ifnull(max(atch_file_seq)+1, 1)} - 게시글 안에서의 다음 순번. */
    @Query("select coalesce(max(f.atchFileSeq), 0) + 1 from CmntySrBbsFile f where f.bbsId = :bbsId")
    int nextSeq(Long bbsId);
}
