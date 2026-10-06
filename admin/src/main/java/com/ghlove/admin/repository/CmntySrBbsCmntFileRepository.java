package com.ghlove.admin.repository;

import com.ghlove.admin.domain.CmntySrBbsCmntFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

/** SR게시판 댓글 첨부파일 (AS-IS {@code G_CMNTY_SR_BBS_CMNT_FILE}). */
public interface CmntySrBbsCmntFileRepository extends JpaRepository<CmntySrBbsCmntFile, Long> {

    /** AS-IS deleteSrBbsCmntFileByCmntId - 댓글 삭제 시 그 댓글의 첨부를 함께 소프트 삭제. */
    List<CmntySrBbsCmntFile> findByCmntIdAndUseYn(Long cmntId, String useYn);

    /** AS-IS deleteSrBbsCmntFileByBbsId - 게시글 삭제 시 댓글 첨부 전체. */
    List<CmntySrBbsCmntFile> findByCmntIdInAndUseYn(Collection<Long> cmntIds, String useYn);

    /** AS-IS insertSrBbsCmntFile의 {@code ifnull(max(atch_file_seq)+1, 1)}. */
    @Query("select coalesce(max(f.atchFileSeq), 0) + 1 from CmntySrBbsCmntFile f where f.cmntId = :cmntId")
    int nextSeq(Long cmntId);
}
