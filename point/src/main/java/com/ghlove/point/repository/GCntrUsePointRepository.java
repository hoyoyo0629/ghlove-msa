package com.ghlove.point.repository;

import com.ghlove.point.domain.GCntrUsePoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface GCntrUsePointRepository extends JpaRepository<GCntrUsePoint, Integer> {

    /** 여러 기부건의 사용이력 - 상세화면이 기부건별 사용액/주문번호를 채운다. */
    List<GCntrUsePoint> findByCntrSnInAndUseSeCode(Collection<String> cntrSns, String useSeCode);

    /** 한 지자체의 사용이력(사용 행) - 상세화면의 구매/사용 거래 행. */
    List<GCntrUsePoint> findByUserIdAndCntrLocgovCodeAndUseSeCode(Long userId, String cntrLocgovCode, String useSeCode);

    /** 주문취소 복원 시 그 주문의 사용이력을 지운다 (AS-IS deleteGiveUsePoint). */
    void deleteByOrderCode(String orderCode);
}
