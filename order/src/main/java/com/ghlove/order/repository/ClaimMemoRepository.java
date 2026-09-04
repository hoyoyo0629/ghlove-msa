package com.ghlove.order.repository;

import com.ghlove.order.domain.ClaimMemo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaimMemoRepository extends JpaRepository<ClaimMemo, Long> {
    List<ClaimMemo> findByClaimIdOrderByCreatedDateDesc(Long claimId);

    List<ClaimMemo> findByClaimIdInOrderByCreatedDateDesc(List<Long> claimIds);
}
