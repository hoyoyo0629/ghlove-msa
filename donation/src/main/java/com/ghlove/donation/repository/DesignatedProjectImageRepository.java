package com.ghlove.donation.repository;

import com.ghlove.donation.domain.DesignatedProjectImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DesignatedProjectImageRepository extends JpaRepository<DesignatedProjectImage, Long> {

    /** 노출 순서대로. 첫 번째가 대표 이미지다. */
    List<DesignatedProjectImage> findByDsgnDntnBizIdOrderByImgSeqAsc(Long dsgnDntnBizId);

    void deleteByDsgnDntnBizId(Long dsgnDntnBizId);
}
