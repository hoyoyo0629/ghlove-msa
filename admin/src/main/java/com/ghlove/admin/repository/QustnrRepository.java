package com.ghlove.admin.repository;

import com.ghlove.admin.domain.Qustnr;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QustnrRepository extends JpaRepository<Qustnr, Long> {
    List<Qustnr> findAllByOrderByQustnrSnDesc();

    /** storefront 공개 노출용 - 노출중(isShow=Y) 설문 최신순. 노출기간(bgnDe~endDe)은 서비스에서 건다. */
    List<Qustnr> findByIsShowOrderByQustnrSnDesc(String isShow);
}
