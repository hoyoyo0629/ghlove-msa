package com.ghlove.gift.repository;

import com.ghlove.gift.domain.GiftOrderStock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GiftOrderStockRepository extends JpaRepository<GiftOrderStock, String> {
}
