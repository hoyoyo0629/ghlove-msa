package com.ghlove.gift.service;

import com.ghlove.gift.domain.Gift;
import com.ghlove.gift.domain.RestockNotice;
import com.ghlove.gift.repository.GiftRepository;
import com.ghlove.gift.repository.RestockNoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 재입고 알림 (AS-IS `RestockNoticeService` / `ItemController:850~896`).
 *
 * <p>품절된 답례품에 회원이 알림을 걸어두는 기능. AS-IS는 상세화면에서
 * `GET /api/item/restock`으로 신청 여부를 확인하고 `POST`로 신청한다.
 */
@Service
@RequiredArgsConstructor
public class RestockNoticeService {

    private static final DateTimeFormatter CREATED_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String NOT_SENT = "N";
    private static final String SOLD_OUT = "1";   // GiftService와 동일 규약 (1=품절, 0=재고있음)

    private final RestockNoticeRepository restockNoticeRepository;
    private final GiftRepository giftRepository;

    /** AS-IS `isRestockNotice` - 이 회원이 이 답례품에 이미 알림을 걸어뒀는가. */
    public boolean isRequested(Integer itemId, Long userId) {
        return restockNoticeRepository.findByItemIdAndUserIdAndSendFlag(itemId, userId, NOT_SENT).isPresent();
    }

    /**
     * 재입고 알림 신청. 품절 상태에서만 의미가 있으므로 재고가 남아 있으면 거절하고,
     * 같은 회원이 중복으로 걸지 못하게 한다(AS-IS는 중복 검사가 없어 같은 신청이 계속
     * 쌓였다 - 여기서는 쌓지 않는다).
     */
    @Transactional
    public void request(Integer itemId, Long userId) {
        Gift gift = giftRepository.findById(itemId.longValue())
                .orElseThrow(() -> new GiftException("답례품을 찾을 수 없습니다."));
        if (!SOLD_OUT.equals(gift.getSoldOut())) {
            throw new GiftException("품절된 답례품에만 재입고 알림을 신청할 수 있습니다.");
        }
        if (isRequested(itemId, userId)) {
            throw new GiftException("이미 재입고 알림을 신청하셨습니다.");
        }
        RestockNotice notice = new RestockNotice();
        notice.setItemId(itemId);
        notice.setUserId(userId);
        notice.setSendFlag(NOT_SENT);
        notice.setCreatedDate(CREATED_FORMAT.format(LocalDateTime.now()));
        restockNoticeRepository.save(notice);
    }
}
