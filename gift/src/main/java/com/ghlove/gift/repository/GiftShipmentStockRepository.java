package com.ghlove.gift.repository;

import com.ghlove.gift.domain.GiftShipmentStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GiftShipmentStockRepository extends JpaRepository<GiftShipmentStock, Long> {

    List<GiftShipmentStock> findByShipmentId(Long shipmentId);

    boolean existsByShipmentId(Long shipmentId);
}
