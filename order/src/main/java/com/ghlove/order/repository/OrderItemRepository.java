package com.ghlove.order.repository;

import com.ghlove.order.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/** 품목(OD_ORDER_ITEM) 리포지토리. Phase 1(가산적 신설)에서는 아직 호출부가 없다. */
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByShipmentId(Long shipmentId);

    List<OrderItem> findByOrderId(String orderId);

    List<OrderItem> findByShipmentIdIn(List<Long> shipmentIds);

    /** AS-IS selectOffOrderList - 기부일련번호(FILLER9)로 아직 처리 안 된(OFF_PROC_YN='N') 오프라인 답례품. */
    List<OrderItem> findByCntrSnAndOffProcYn(String cntrSn, String offProcYn);

    /** 배치 cntrSunapBatch - 아직 처리 안 된(OFF_PROC_YN='N') 오프라인 답례품의 기부일련번호들. */
    @org.springframework.data.jpa.repository.Query(
            "select distinct i.cntrSn from OrderItem i where i.cntrSn is not null and i.offProcYn = :offProcYn")
    List<String> findDistinctCntrSnByOffProcYn(@org.springframework.data.repository.query.Param("offProcYn") String offProcYn);

    List<OrderItem> findByCntrSnAndItemStatus(String cntrSn, String itemStatus);

    List<OrderItem> findByCntrSnOrderByOrderItemIdAsc(String cntrSn);

    /**
     * AS-IS offgive-mapper selectOffItem의 주문 쪽 조건 - 기부일련번호(FILLER9)가 있는 해당 상태 품목 중
     * 생성일이 [from, to)인 것의 기부일련번호(중복 제거). 기부 쪽 조건(오프라인·미납·삭제)은 donation이 본다.
     */
    @Query("select distinct i.cntrSn from OrderItem i where i.cntrSn is not null and i.cntrSn <> ''"
            + " and i.itemStatus = :itemStatus and i.createdDate >= :from and i.createdDate < :to")
    List<String> findCntrSnsByItemStatusCreatedBetween(@Param("itemStatus") String itemStatus,
                                                       @Param("from") LocalDateTime from,
                                                       @Param("to") LocalDateTime to);
}
