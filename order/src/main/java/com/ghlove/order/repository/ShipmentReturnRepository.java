package com.ghlove.order.repository;

import com.ghlove.order.domain.ShipmentReturn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShipmentReturnRepository extends JpaRepository<ShipmentReturn, Integer> {
    List<ShipmentReturn> findBySellerIdOrderByDefaultAddressFlagDescShipmentReturnIdDesc(Long sellerId);

    List<ShipmentReturn> findAllByOrderByShipmentReturnIdDesc();
}
