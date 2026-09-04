package com.ghlove.order.repository;

import com.ghlove.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/** JpaSpecificationExecutor: admin 주문관리 콘솔의 다중 검색조건(주문번호/수취인명/답례품명/
 * 판매자ID/주문자ID/상태/지자체/기간)을 OrderAdminService.OrderSearchSpecs가 동적으로
 * 조합한다 - CntrReqmngRepository.search()의 nullable-JPQL 패턴은 IN절(판매자ID 목록)까지
 * 자연스럽게 표현하기 어려워 여기서는 Specification을 쓴다. */
public interface OrderRepository extends JpaRepository<Order, String>, JpaSpecificationExecutor<Order> {
    List<Order> findByUserIdOrderByCreatedDateDesc(Long userId);

    List<Order> findByOrderIdIn(List<String> orderIds);

    List<Order> findByPointOutcomeOrderByCreatedDateDesc(String pointOutcome);
}
