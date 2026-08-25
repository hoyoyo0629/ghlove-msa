package com.ghlove.order.repository;

import com.ghlove.order.domain.Claim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClaimRepository extends JpaRepository<Claim, Long> {
    List<Claim> findByStatusOrderByClaimIdDesc(String status);

    Optional<Claim> findByOrderIdAndStatus(String orderId, String status);

    List<Claim> findByOrderIdIn(List<String> orderIds);
}
