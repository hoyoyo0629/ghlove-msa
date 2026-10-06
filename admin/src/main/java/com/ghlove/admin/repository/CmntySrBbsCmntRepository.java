package com.ghlove.admin.repository;

import com.ghlove.admin.domain.CmntySrBbsCmnt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * SR게시판 댓글 (AS-IS {@code G_CMNTY_SR_BBS_CMNT}). 상세화면 목록은 작성자 소속·첨부파일까지
 * 붙여야 해서 {@link CmntySrBbsAdminRepository#cmntList(long)}가 담당한다.
 */
public interface CmntySrBbsCmntRepository extends JpaRepository<CmntySrBbsCmnt, Long> {

    /** AS-IS selectSrBbsCmntDetail - 살아있는 댓글만. */
    Optional<CmntySrBbsCmnt> findByCmntIdAndUseYn(Long cmntId, String useYn);

    /** 게시글 삭제 시 댓글을 함께 소프트 삭제하기 위한 조회(AS-IS deleteSrBbsCmntByBbsId). */
    List<CmntySrBbsCmnt> findByBbsIdAndUseYn(Long bbsId, String useYn);
}
