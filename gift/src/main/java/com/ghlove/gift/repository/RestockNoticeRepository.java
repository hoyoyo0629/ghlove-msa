package com.ghlove.gift.repository;

import com.ghlove.gift.domain.RestockNotice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** 재입고 알림 신청 (AS-IS `OP_RESTOCK_NOTICE`). */
public interface RestockNoticeRepository extends JpaRepository<RestockNotice, Long> {

    /** 이미 신청했는지 - 아직 발송되지 않은(SEND_FLAG='N') 신청만 "신청 중"으로 본다. */
    Optional<RestockNotice> findByItemIdAndUserIdAndSendFlag(Integer itemId, Long userId, String sendFlag);

    List<RestockNotice> findByItemIdAndSendFlag(Integer itemId, String sendFlag);
}
