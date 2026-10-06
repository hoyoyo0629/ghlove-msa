package com.ghlove.admin.repository;

import com.ghlove.admin.domain.CmntyCmnt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 소통방 댓글 (AS-IS {@code G_CMNTY_CMNT}). 목록 조회는 작성자 소속까지 붙여야 해서
 * {@link CmntyBbsAdminRepository#bbsCmntList(long)}가 담당하고, 여기는 단건 조회·저장만 쓴다.
 */
public interface CmntyCmntRepository extends JpaRepository<CmntyCmnt, Long> {

    /** AS-IS detailCmnt - {@code use_yn='Y'}인 것만 찾는다(삭제된 댓글은 없는 것으로 본다). */
    Optional<CmntyCmnt> findByCmntIdAndUseYn(Long cmntId, String useYn);
}
