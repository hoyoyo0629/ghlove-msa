package com.ghlove.donation.repository;

import com.ghlove.donation.domain.DesignatedProjectImgExpln;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DesignatedProjectImgExplnRepository
        extends JpaRepository<DesignatedProjectImgExpln, Long> {

    List<DesignatedProjectImgExpln> findByDsgnDntnBizIdOrderByImgSeqAsc(Long dsgnDntnBizId);

    void deleteByDsgnDntnBizId(Long dsgnDntnBizId);
}
