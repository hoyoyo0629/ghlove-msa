package com.ghlove.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.springframework.util.StringUtils.hasLength;

/**
 * 답례품승인관리(AS-IS ItemServiceImpl.updateListData)의 국민비서 알림 - 승인처리(ITEM_APPROVAL_COMPLETE) / 등록보류(ITEM_APPROVAL_CANCEL)를
 * 답례품 제공자에게 보낸다. 수신정보는 AS-IS getUserIdByItemId(OP_SELLER ⋈ OP_SELLER_USER)와 같은 gift의 sms-receiver.
 *
 * <p>AS-IS와 같은 점 - 휴대폰·본인확인(CI)이 있고 수신동의가 "0"일 때만 보낸다. 승인 쪽은 휴대폰에서 '-'를 뗀 값으로 비었는지 보고, 등록보류 쪽은
 * 뗀 값이 아니라 원래 값으로 본다. 삭제신청 승인(deleted)은 문자가 없다. 한 건의 실패(NPE·ClassCast)는 로그만 남기고 다음 건으로 간다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ItemApprovalSmsService {

    private static final String RECEIVE_SMS_AGREED = "0";

    private final GiftClient giftClient;
    private final SmsIpsService smsIpsService;

    /** 승인처리 - 내용: 상호명|승인요청일시|상품코드|휴대폰. */
    public void approved(List<ItemManageClient.ProcessedItem> items) {
        for (ItemManageClient.ProcessedItem item : items) {
            if (item.deleted()) {
                continue;
            }
            try {
                GiftClient.SellerSmsReceiver seller = Objects.requireNonNull(giftClient.sellerSmsReceiver(item.sellerId()));
                if (hasLength(seller.phoneNumber().replaceAll("-", "")) && hasLength(seller.mberCi())
                        && RECEIVE_SMS_AGREED.equals(seller.receiveSms())) {
                    String contents = seller.userName() + "|" + PresentOrderSmsService.localDateTimeToStr(LocalDateTime.now())
                            + "|" + item.itemUserCode() + "|" + seller.phoneNumber().replaceAll("-", "");
                    smsIpsService.insertTifIpsSndngM(SmsType.ITEM_APPROVAL_COMPLETE, seller.mberCi(), contents);
                }
            } catch (RuntimeException e) {
                log.error("{} :: updateItemApproval send sms error", getClass().getName(), e);
            }
        }
    }

    /** 등록보류 - 내용: 상호명|승인요청일시|등록보류사유|휴대폰. */
    public void rejected(List<ItemManageClient.ProcessedItem> items, String reason) {
        for (ItemManageClient.ProcessedItem item : items) {
            try {
                GiftClient.SellerSmsReceiver seller = Objects.requireNonNull(giftClient.sellerSmsReceiver(item.sellerId()));
                if (hasLength(seller.phoneNumber()) && hasLength(seller.mberCi()) && RECEIVE_SMS_AGREED.equals(seller.receiveSms())) {
                    String contents = seller.userName() + "|" + PresentOrderSmsService.localDateTimeToStr(LocalDateTime.now())
                            + "|" + reason + "|" + seller.phoneNumber().replaceAll("-", "");
                    smsIpsService.insertTifIpsSndngM(SmsType.ITEM_APPROVAL_CANCEL, seller.mberCi(), contents);
                }
            } catch (RuntimeException e) {
                log.error("{} :: updateItemApproval send sms error", getClass().getName(), e);
            }
        }
    }
}
