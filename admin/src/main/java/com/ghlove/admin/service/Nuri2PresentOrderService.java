package com.ghlove.admin.service;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClient;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 배치 sendNuri2PresentOrder(답례품 주문내역 알림톡, AS-IS 매일 09:00) - AS-IS {@code JobServiceImpl.sendNuri2PresentOrder}.
 * 판매자별 결제완료 답례품 건수(order)를 사용(AS-IS STATUS_CODE '2') 판매자의 휴대폰으로, 누리2 발송대기 표
 * {@code NURI2_NRMSG_DATA}에 알림톡(템플릿 KR002) 한 건씩 넣는다 - 실제 발송은 누리2 에이전트가 이 표를 읽어 한다.
 * 값·문구는 AS-IS {@code Nuri2ServiceImpl.insertAlimtalk} / {@code Nuri2NrmsgData.getOrderContent} 그대로.
 */
@Service
@Slf4j
public class Nuri2PresentOrderService {

    private static final String SELLER_ACTIVE = "2";

    private final GiftClient giftClient;
    private final EntityManager entityManager;
    private final TransactionTemplate tx;
    private final RestClient order;
    private final String senderKey;

    public record SellerOrderCount(Long sellerId, long totalCnt) {
    }

    public Nuri2PresentOrderService(GiftClient giftClient, EntityManager entityManager, TransactionTemplate tx,
                                    @Value("${ghlove.order-service.base-url}") String orderBaseUrl,
                                    @Value("${ghlove.internal.admin-secret}") String adminSecret,
                                    @Value("${ghlove.integrations.nuri2.sender-key:}") String senderKey) {
        this.giftClient = giftClient;
        this.entityManager = entityManager;
        this.tx = tx;
        this.order = RestClient.builder().baseUrl(orderBaseUrl).defaultHeader("X-Internal-Secret", adminSecret).build();
        this.senderKey = senderKey;
    }

    public String send() {
        List<SellerOrderCount> counts = order.get().uri("/api/admin/orders/batch/present-order-counts").retrieve()
                .body(new ParameterizedTypeReference<List<SellerOrderCount>>() {
                });
        if (counts == null) {
            counts = List.of();
        }
        Map<Long, GiftClient.SellerDetail> sellers = giftClient.sellers().stream()
                .collect(Collectors.toMap(GiftClient.SellerDetail::sellerId, Function.identity(), (a, b) -> a));
        int inserted = 0;
        for (SellerOrderCount c : counts) {
            GiftClient.SellerDetail seller = sellers.get(c.sellerId());
            // AS-IS: JOIN op_seller ... AND OS.STATUS_CODE = '2'
            if (seller == null || !SELLER_ACTIVE.equals(seller.statusCode())) {
                continue;
            }
            if (seller.phoneNumber() == null || seller.phoneNumber().isBlank()) {
                // AS-IS는 PHONE_NUM이 null이면 toString()에서 NPE가 나 배치 전체가 멈춘다 - 그 판매자만 건너뛴다.
                log.error("■■■■■■■■ sendNuri2PresentOrder 휴대폰 없음 sellerId={}", c.sellerId());
                continue;
            }
            String content = orderContent(String.valueOf(c.totalCnt()));
            try {
                tx.executeWithoutResult(t -> entityManager.createNativeQuery("""
                                insert into admin.nuri2_nrmsg_data (msg_state, input_date, res_date, alt_country_code, phone,
                                    callback, msg_type_1, contents_type_1, alt_sender_key, alt_template_code, alt_json,
                                    msg_type_2, contents_type_2, xms_subject, xms_text)
                                values (1, now(), now(), '82', :phone, '15222431', 'ALT', 'ALT', :senderKey, 'KR002', :altJson,
                                    'MMS', 'LMS', '', :xmsText)
                                """)
                        .setParameter("phone", seller.phoneNumber())
                        .setParameter("senderKey", senderKey)
                        .setParameter("altJson", content)
                        .setParameter("xmsText", content)
                        .executeUpdate());
                inserted++;
            } catch (RuntimeException e) {
                log.error("■■■■■■■■ sendNuri2PresentOrder Error ■■■■■■■■, [배치] : [{}]", e.toString());
            }
        }
        log.info("■■■■■■■■ sendNuri2PresentOrder Success");
        return "판매자 " + counts.size() + "곳 / 알림톡 등록 " + inserted + "건";
    }

    /** AS-IS Nuri2NrmsgData.getOrderContent("TOTAL_PRESENT_ORDER", 건수). */
    static String orderContent(String orderCnt) {
        return "[고향사랑e음 답례품 주문 알림]\n"
                + "  - 결제완료된 답례품 주문 내역을 안내드립니다.\n"
                + "\n"
                + "  ∙ 주문 일자 : " + localDateTimeToStr(LocalDateTime.now()) + "\n"
                + "  ∙ 총 주문건수 : " + orderCnt + "건\n"
                + "\n"
                + "상세 주문내역을 확인하시어 출고를 진행하여 주시기 바랍니다."
                + "\n"
                + "☎ 주문내역 문의 : 1522-2431";
    }

    /** AS-IS getLocalDateTimeToStr - "2026.05.18.(월) ~ 2026.05.19.(화) 오전 08:00". 오후는 12시간제 시각(hh). */
    static String localDateTimeToStr(LocalDateTime now) {
        LocalDateTime yesterday = now.minusDays(1);
        String ymd = now.toLocalDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd."));
        String yesterdayYmd = yesterday.toLocalDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd."));
        String time;
        String hm;
        if (now.getHour() < 12) {
            time = "오전";
            hm = now.toLocalTime().format(DateTimeFormatter.ofPattern("HH")) + ":00";
        } else {
            time = "오후";
            hm = now.toLocalTime().format(DateTimeFormatter.ofPattern("hh")) + ":00";
        }
        return yesterdayYmd + "(" + weekStr(yesterday.getDayOfWeek()) + ") ~ " + ymd + "(" + weekStr(now.getDayOfWeek()) + ") "
                + time + " " + hm;
    }

    private static String weekStr(DayOfWeek week) {
        return switch (week) {
            case MONDAY -> "월";
            case TUESDAY -> "화";
            case WEDNESDAY -> "수";
            case THURSDAY -> "목";
            case FRIDAY -> "금";
            case SATURDAY -> "토";
            case SUNDAY -> "일";
        };
    }
}
