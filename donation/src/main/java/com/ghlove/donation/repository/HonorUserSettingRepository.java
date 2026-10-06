package com.ghlove.donation.repository;

import com.ghlove.donation.domain.LclgvHnrUserStngMng;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** 지자체별 기부혜택증 설정 (AS-IS G_LCLGV_HNR_USER_STNG_MNG) - admin 메뉴 19101·19102용. */
public interface HonorUserSettingRepository extends JpaRepository<LclgvHnrUserStngMng, String> {

    /** AS-IS 목록 정렬은 지자체코드 오름차순이다(ROW_NUMBER() OVER(ORDER BY LCLGV_CD)). */
    List<LclgvHnrUserStngMng> findAllByOrderByLclgvCdAsc();
}
