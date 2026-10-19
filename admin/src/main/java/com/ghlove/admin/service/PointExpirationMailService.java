package com.ghlove.admin.service;

import com.ghlove.admin.domain.MailConfig;
import com.ghlove.admin.service.integration.EmsMailClient;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.HtmlUtils;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 배치 expirationPointSendMessageBatch("기부 포인트 만료 안내 메일발송", AS-IS 02:30, AS-IS에서 중지 상태) -
 * AS-IS {@code GivePointExpirationServiceImpl.givePointExpirationMailSend}: 만료 90일 전인 잔여 포인트를 회원·만료일별로 모아
 * 템플릿 {@code expiration_point}로 한 명씩 EMS 발송한다. 템플릿이 없거나 구매자 발송(buyer_send_flag)이 'Y'가 아니면
 * AS-IS처럼 보내지 않는다(현재 AS-IS 실데이터가 'N'이라 실제로는 발송되지 않는다).
 * 대상 집계는 point(적립 원장), 이름·이메일은 member, 템플릿·상점명·EMS는 admin.
 */
@Service
@Slf4j
public class PointExpirationMailService {

    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter KOREAN_DATE = DateTimeFormatter.ofPattern("yyyy년MM월dd일");

    public record ExpiringPoint(Long userId, String pointEndDe, Long point) {
    }

    public record MailTarget(Long userId, String loginId, String userName, String email, String statusCode) {
    }

    private final MailConfigService mailConfigService;
    private final EmsMailClient emsMailClient;
    private final EntityManager entityManager;
    private final RestClient point;
    private final RestClient member;

    public PointExpirationMailService(MailConfigService mailConfigService, EmsMailClient emsMailClient, EntityManager entityManager,
                                      @Value("${ghlove.point-service.base-url}") String pointBaseUrl,
                                      @Value("${ghlove.member-service.base-url}") String memberBaseUrl,
                                      @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        this.mailConfigService = mailConfigService;
        this.emsMailClient = emsMailClient;
        this.entityManager = entityManager;
        this.point = RestClient.builder().baseUrl(pointBaseUrl).defaultHeader("X-Internal-Secret", adminSecret).build();
        this.member = RestClient.builder().baseUrl(memberBaseUrl).defaultHeader("X-Internal-Secret", adminSecret).build();
    }

    public String send() {
        List<ExpiringPoint> list = point.get().uri("/api/admin/batch/expiring-points").retrieve()
                .body(new ParameterizedTypeReference<List<ExpiringPoint>>() {
                });
        if (list == null || list.isEmpty()) {
            return "대상 0건";
        }
        List<Long> userIds = list.stream().map(ExpiringPoint::userId).distinct().toList();
        List<MailTarget> targets = member.get().uri(b -> b.path("/api/admin/members/batch/mail-targets")
                        .queryParam("userIds", userIds).build()).retrieve()
                .body(new ParameterizedTypeReference<List<MailTarget>>() {
                });
        // AS-IS INNER JOIN OP_USER(정상·휴면) - 그 외 상태 회원은 목록에서 빠진다
        Map<Long, MailTarget> users = targets == null ? Map.of()
                : targets.stream().collect(Collectors.toMap(MailTarget::userId, Function.identity()));
        String siteName = shopName();

        int sent = 0;
        int skipped = 0;
        for (ExpiringPoint p : list) {
            MailTarget user = users.get(p.userId());
            if (user == null) {
                continue;
            }
            try {
                // 0. 유효성 검사
                if (isEmpty(user.userName()) || isEmpty(user.email())) {
                    skipped++;
                    continue;
                }
                // 1. 메일 템플릿 조회
                MailConfig mailConfig = mailConfigService.findByTemplateId("expiration_point");
                if (mailConfig == null || !"Y".equals(mailConfig.getBuyerSendFlag())) {
                    skipped++;
                    continue;
                }
                // 3. 메일 템플릿 대체코드 변환 적용(AS-IS MemberExpirationPointMail - 키는 {snake_case})
                Map<String, String> map = new LinkedHashMap<>();
                map.put("{email}", user.email());
                map.put("{id}", user.loginId());
                map.put("{name}", user.userName());
                map.put("{expiration_date}", expirationDate(p.pointEndDe()));
                map.put("{search_date}", LocalDate.now().format(KOREAN_DATE));
                map.put("{site_name}", siteName);
                long amount = p.point() == null ? 0 : p.point();
                map.put("{expiration_point}", amount > 0 ? new DecimalFormat("###,###").format(amount) : String.valueOf(p.point()));
                String subject = replace(mailConfig.getBuyerSubject(), map);
                String content = replace(mailConfig.getBuyerContent(), map);

                // 4~6. EMS 발송(AS-IS: 즉시, 분류·링크명 "포인트만료")
                emsMailClient.sendMail(HtmlUtils.htmlUnescape(subject), HtmlUtils.htmlUnescape(content), "", "",
                        user.email() + " " + user.userName(), "포인트만료", "포인트만료", "");
                sent++;
            } catch (Exception e) {
                // AS-IS도 한 명 실패가 나머지 발송을 막지 않는다(로그만)
                log.error("메일발송 송출 에러::: {}", e.toString());
            }
        }
        return "대상 " + list.size() + "건 / 발송 " + sent + "건 / 제외 " + skipped + "건";
    }

    /** AS-IS {@code DATE_FORMAT(DATE_SUB(POINT_END_DE, INTERVAL -1 DAY), '%Y년%m월%d일')} - 만료일 다음 날. */
    private static String expirationDate(String pointEndDe) {
        try {
            return LocalDate.parse(pointEndDe, YMD).plusDays(1).format(KOREAN_DATE);
        } catch (RuntimeException e) {
            return "";
        }
    }

    @SuppressWarnings("unchecked")
    private String shopName() {
        List<Object> rows = entityManager.createNativeQuery("select shop_name from admin.op_config where shop_config_id = 1").getResultList();
        return rows.isEmpty() || rows.get(0) == null ? "" : rows.get(0).toString();
    }

    private static String replace(String text, Map<String, String> map) {
        String s = text == null ? "" : text;
        for (Map.Entry<String, String> e : map.entrySet()) {
            String v = e.getValue() == null || "null".equals(e.getValue()) ? "" : e.getValue();
            s = s.replace(e.getKey(), v);
        }
        return s;
    }

    private static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }
}
