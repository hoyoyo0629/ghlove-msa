package com.ghlove.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 국민비서 "답례품 주문시"(PRESENT_ORDER, 812-A004) 문자 - AS-IS {@code OrderServiceImpl.saveGiveOrderTempAndPay}
 * (온라인 주문)와 {@code NgDonationBatchServiceImpl.procOffOrder}(기탁서 등록 답례품)의 "국민비서 알림 전송 - 구매자"·
 * "국민비서 알림 전송 - 답례품 제공자" 블록 이식(두 곳의 발송 규칙은 같다). 답례품 주문이 결제완료되면 order가 보내는
 * 이벤트로 불린다.
 *
 * <p><b>AS-IS 규칙 그대로</b>:
 * <ul>
 *   <li>발송내용 {@code 이름|신청일시|주문번호|상품명|수신전화번호} - 상품명은 첫 품목의 이름(HTML 해제, '|'→'-')에
 *       옵션 표시문자열이 있고 '|'가 없으면 {@code (옵션)}을 붙이고, 품목이 둘 이상이면 " 등"을 붙인다(구매자).
 *       전화번호는 '-'를 뺀다.</li>
 *   <li>구매자: 수신동의 '0'·휴대폰·CI가 다 있을 때만 보낸다. 이름은 구매자 이름.</li>
 *   <li>답례품 제공자: 같은 (판매자, 답례품)은 한 번만. 업체 휴대폰·판매자 계정 CI·수신동의 '0'일 때만 보내고,
 *       이름 칸에는 <b>구매자 이름</b>이 들어간다(AS-IS 변수 userName). 상품명에는 " 등"이 붙지 않는다.</li>
 *   <li>수신자 한 명의 조회가 실패해도 나머지는 계속 보낸다(AS-IS는 수신자별로 NPE를 잡는다).</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PresentOrderSmsService {

    /** AS-IS 수신동의 값 - 이 값일 때만 보낸다. */
    private static final String RECEIVE_SMS_AGREED = "0";

    private final SmsIpsService smsIpsService;
    private final MemberAdminClient memberAdminClient;
    private final GiftClient giftClient;

    /** AS-IS BuyItem 중 문자에 쓰는 값 - optionsDisplay는 AS-IS OP_ORDER_ITEM.OPTIONS. */
    public record BuyItem(Long itemId, Long sellerId, String itemName, String optionsDisplay) {
    }

    public void send(String orderCode, Long buyUserId, List<BuyItem> buyItems, LocalDateTime msgDateTime) {
        if (buyItems == null || buyItems.isEmpty()) {
            return;
        }
        String date = localDateTimeToStr(msgDateTime != null ? msgDateTime : LocalDateTime.now());

        // 국민비서 알림 전송 - 구매자
        String userName = "";
        try {
            MemberAdminClient.SmsReceiver user = memberAdminClient.smsReceiver(buyUserId);
            userName = Objects.requireNonNull(user).userName();
            if (RECEIVE_SMS_AGREED.equals(user.receiveSms()) && hasLength(user.phoneNumber())
                    && hasLength(user.mberCi())) {
                BuyItem buyItem = buyItems.get(0);
                String contents = user.userName() + "|" + date + "|" + orderCode + "|" + itemText(buyItem)
                        + (buyItems.size() > 1 ? " 등" : "")
                        + "|" + user.phoneNumber().replaceAll("-", "");
                smsIpsService.insertTifIpsSndngM(SmsType.PRESENT_ORDER, user.mberCi(), contents);
            }
        } catch (RuntimeException e) {
            log.error("OrderCode : {} send user sms error", orderCode, e);
        }

        // 같은 상품 여러 건일 경우 한번만 발송(답례품 제공자)
        List<BuyItem> sendSmsItemList = new ArrayList<>();
        for (BuyItem buyItem : buyItems) {
            boolean exist = sendSmsItemList.stream().anyMatch(s -> Objects.equals(s.sellerId(), buyItem.sellerId())
                    && Objects.equals(s.itemId(), buyItem.itemId()));
            if (!exist) {
                sendSmsItemList.add(buyItem);
            }
        }

        // 국민비서 알림 전송 - 답례품 제공자
        for (BuyItem sendSmsItem : sendSmsItemList) {
            try {
                GiftClient.SellerSmsReceiver sellerUser =
                        Objects.requireNonNull(giftClient.sellerSmsReceiver(sendSmsItem.sellerId()));
                if (RECEIVE_SMS_AGREED.equals(sellerUser.receiveSms()) && hasLength(sellerUser.phoneNumber())
                        && hasLength(sellerUser.mberCi())) {
                    String contents = userName + "|" + date + "|" + orderCode + "|" + itemText(sendSmsItem)
                            + "|" + sellerUser.phoneNumber().replaceAll("-", "");
                    smsIpsService.insertTifIpsSndngM(SmsType.PRESENT_ORDER, sellerUser.mberCi(), contents);
                }
            } catch (RuntimeException e) {
                log.error("OrderCode : {} send seller sms error :: sellerId :: {}", orderCode, sendSmsItem.sellerId(), e);
            }
        }
    }

    /** AS-IS 상품명 칸 - 이름(HTML 해제, '|'→'-') + 옵션 표시문자열에 '|'가 없으면 "(옵션)". */
    private static String itemText(BuyItem item) {
        StringBuilder sb = new StringBuilder();
        sb.append(HtmlUtils.htmlUnescape(item.itemName()).replaceAll("\\|", "-"));
        if (hasLength(item.optionsDisplay()) && !item.optionsDisplay().contains("|")) {
            sb.append("(");
            sb.append(HtmlUtils.htmlUnescape(item.optionsDisplay()));
            sb.append(")");
        }
        return sb.toString();
    }

    /** AS-IS {@code ReceiverInfo.getLocalDateTimeToStr} - "2023.02.16.(목) 오후 01:30"(오전은 HH:mm, 오후는 hh:mm). */
    static String localDateTimeToStr(LocalDateTime msgDateTime) {
        String ymd = msgDateTime.toLocalDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd."));
        String hm = msgDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        DayOfWeek week = msgDateTime.getDayOfWeek();
        String weekStr = switch (week) {
            case MONDAY -> "월";
            case TUESDAY -> "화";
            case WEDNESDAY -> "수";
            case THURSDAY -> "목";
            case FRIDAY -> "금";
            case SATURDAY -> "토";
            case SUNDAY -> "일";
        };
        String time;
        if (msgDateTime.getHour() < 12) {
            time = "오전";
        } else {
            time = "오후";
            hm = msgDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm"));
        }
        return ymd + "(" + weekStr + ") " + time + " " + hm;
    }

    private static boolean hasLength(String value) {
        return value != null && !value.isEmpty();
    }
}
