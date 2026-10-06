package com.ghlove.order.repository;

import com.ghlove.order.domain.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** 출고(OD_SHIPMENT) 리포지토리. Phase 1(가산적 신설)에서는 아직 호출부가 없다 -
 *  체크아웃/SAGA가 신 모델로 전환되는 Phase 2 이후 사용된다. */
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    List<Shipment> findByOrderId(String orderId);

    List<Shipment> findByOrderIdIn(List<String> orderIds);
}
