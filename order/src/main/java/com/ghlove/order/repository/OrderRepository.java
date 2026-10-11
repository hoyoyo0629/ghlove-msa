package com.ghlove.order.repository;

import com.ghlove.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/** JpaSpecificationExecutor: admin 주문관리 콘솔의 다중 검색조건(주문번호/수취인명/답례품명/
 * 판매자ID/주문자ID/상태/지자체/기간)을 OrderAdminService.OrderSearchSpecs가 동적으로
 * 조합한다 - CntrReqmngRepository.search()의 nullable-JPQL 패턴은 IN절(판매자ID 목록)까지
 * 자연스럽게 표현하기 어려워 여기서는 Specification을 쓴다. */
public interface OrderRepository extends JpaRepository<Order, String>, JpaSpecificationExecutor<Order> {
    List<Order> findByUserIdOrderByCreatedDateDesc(Long userId);

    List<Order> findByOrderIdIn(List<String> orderIds);

    List<Order> findByPointOutcomeOrderByCreatedDateDesc(String pointOutcome);

    /**
     * AS-IS {@code OrderMapper.getOrderCodeNum}과 같은 역할 - 주문번호의 숫자부분을 원자적으로
     * 하나씩 채번한다(migration-order-code-asis-format.sql이 기존 19건을 옮긴 뒤 19로 맞춰
     * 둬서 다음 호출이 20부터 이어진다).
     */
    @Query(value = "select nextval('ord.op_order_code_seq')", nativeQuery = true)
    long nextOrderCodeSeq();

    /**
     * AS-IS {@code OrderAdminServiceImpl.getNewOrderCode}("A" + 프레임워크 시퀀스, 실데이터는
     * "AB"+yyMMdd+8자리)와 같은 역할 - 관리자 수기/엑셀 주문등록 전용 코드의 날짜별 순번을
     * 매긴다. CUBRID 프레임워크 시퀀스의 정확한 포맷은 소스에 없어 재현 못 했지만, 관찰된
     * 모양("AB"+yyMMdd+8자리)은 그대로 맞췄다 - [[order-code-format-matches-asis]] 참고.
     */
    @Query(value = "select coalesce(max(cast(substring(order_id from 9 for 8) as bigint)), 0) + 1 "
            + "from ord.od_order where order_id like concat('AB', :datePrefix, '%')", nativeQuery = true)
    long nextAdminOrderSeqForDate(@Param("datePrefix") String datePrefix);
}
