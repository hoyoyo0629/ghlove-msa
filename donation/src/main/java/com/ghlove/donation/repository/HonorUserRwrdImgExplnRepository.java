package com.ghlove.donation.repository;

import com.ghlove.donation.domain.LclgvHnrUserRwrdImgExpln;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 기부혜택증 메인 이미지 설명 (AS-IS G_LCLGV_HNR_USER_RWRD_IMG_EXPLN). */
public interface HonorUserRwrdImgExplnRepository
        extends JpaRepository<LclgvHnrUserRwrdImgExpln, LclgvHnrUserRwrdImgExpln.Key> {

    List<LclgvHnrUserRwrdImgExpln> findByLclgvCdOrderByImgSeqAsc(String lclgvCd);

    /** AS-IS는 저장할 때 지자체 단위로 전부 지우고 다시 넣는다(deleteImgDescListByPrjId). */
    @Transactional
    void deleteByLclgvCd(String lclgvCd);
}
