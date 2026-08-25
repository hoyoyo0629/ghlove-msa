package com.ghlove.gift.repository;

import com.ghlove.gift.domain.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SellerRepository extends JpaRepository<Seller, Long> {
    List<Seller> findByCommunityBusinessYn(String communityBusinessYn);

    List<Seller> findAllByOrderBySellerIdDesc();
}
