package com.ghlove.point.repository;

import com.ghlove.point.domain.GCntrUsePoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface GCntrUsePointRepository extends JpaRepository<GCntrUsePoint, Integer> {

    /** 여러 기부건의 사용이력 - 상세화면이 기부건별 사용액/주문번호를 채운다. */
    List<GCntrUsePoint> findByCntrSnInAndUseSeCode(Collection<String> cntrSns, String useSeCode);

    /** 한 지자체의 사용이력(사용 행) - 상세화면의 구매/사용 거래 행. */
    List<GCntrUsePoint> findByUserIdAndCntrLocgovCodeAndUseSeCode(Long userId, String cntrLocgovCode, String useSeCode);

    /** 주문취소 복원 시 그 주문의 사용이력을 지운다 (AS-IS deleteGiveUsePoint). */
    void deleteByOrderCode(String orderCode);

    /** 회원 한 명의 포인트 사용이력 전체 - admin 일반회원관리(메뉴 4101) 상세의 포인트 내역용. */
    List<GCntrUsePoint> findByUserId(Long userId);

    /**
     * 회원별 사용포인트 합계 - admin 일반회원관리(4101) 목록의 "포인트잔액"(발생 - 사용)과
     * 상세 누적합계의 "사용포인트" 칸용이다. AS-IS
     * {@code getGeneralCustomerCumulativeTotal}의 서브쿼리처럼 <b>USE_SE_CODE='1'(사용)만</b>
     * 합산한다(소멸='3' 등은 제외).
     */
    @Query(value = """
            select p.user_id, coalesce(sum(p.cntr_use_point), 0)
              from g_cntr_use_point p
             where p.user_id in (:userIds)
               and p.use_se_code = '1'
             group by p.user_id
            """, nativeQuery = true)
    List<Object[]> sumUsedPointByUserIds(@Param("userIds") Collection<Long> userIds);

    /**
     * 포인트사용 정합성검증(admin 메뉴 7210)의 "사용완료 포인트" - AS-IS
     * {@code pointcheck-mapper}의 {@code G_CNTR_USE_POINT} 파트를 그대로 옮긴 것이다
     * ({@code SUM(CASE WHEN USE_SE_CODE='1' THEN CNTR_USE_POINT ELSE 0 END)}을 주문번호로 묶는다).
     *
     * 멀티아이템 주문은 ORDER_CODE에 {@code 주문번호#항목번호}가 들어가므로({@code consumeLots}가
     * 그렇게 기록한다) {@code #} 앞부분으로 묶어 주문 단위로 맞춘다 - AS-IS는 ORDER_CODE가
     * 주문번호 그대로였다.
     */
    @Query(value = """
            select split_part(p.order_code, '#', 1) as order_code
                 , sum(case when p.use_se_code = '1' then cast(p.cntr_use_point as bigint) else 0 end) as used_point
              from g_cntr_use_point p
             where p.order_code is not null
               and p.order_code <> ''
               and (cast(:query as varchar) is null or p.order_code like concat('%', :query, '%'))
             group by split_part(p.order_code, '#', 1)
            """, nativeQuery = true)
    List<Object[]> sumUsedPointByOrderCode(@Param("query") String query);
}
