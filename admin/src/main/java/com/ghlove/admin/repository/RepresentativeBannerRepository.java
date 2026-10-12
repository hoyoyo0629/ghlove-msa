package com.ghlove.admin.repository;

import com.ghlove.admin.domain.RepresentativeBanner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RepresentativeBannerRepository extends JpaRepository<RepresentativeBanner, Integer> {

    List<RepresentativeBanner> findByProcessTypeOrderByDisplayOrderAsc(String processType);

    /**
     * AS-IS {@code insertRepresentativeBanner}의
     * {@code (SELECT NVL(MAX(DISPLAY_ORDER),0)+1 FROM G_REPRST_BANNER)} 재현.
     * AS-IS가 PROCESS_TYPE을 걸지 않고 테이블 전체의 MAX를 쓰는 것까지 그대로 둔다.
     */
    @Query("select coalesce(max(b.displayOrder), 0) from RepresentativeBanner b")
    int findMaxDisplayOrder();
}
