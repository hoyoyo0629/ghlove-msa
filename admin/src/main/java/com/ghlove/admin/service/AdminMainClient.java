package com.ghlove.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * 관리자 메인화면(AS-IS {@code /opmanager} main/index.jsp) 집계 조회대행 - AS-IS는 한 DB에서 UNION ALL 한 방이지만
 * TO-BE는 회원(member)·기부(donation)·포인트 사용(point)·주문(order)·답례품(gift)이 서비스마다 나뉘어 있어
 * 각 서비스의 {@code /api/admin/.../dashboard} 를 부른다. 실패 처리는 호출하는 {@link AdminMainService}가 정한다.
 */
@Component
public class AdminMainClient {

    private final RestClient member;
    private final RestClient donation;
    private final RestClient point;
    private final RestClient order;
    private final RestClient gift;

    public AdminMainClient(@Value("${ghlove.member-service.base-url}") String memberBaseUrl,
                           @Value("${ghlove.donation-service.base-url}") String donationBaseUrl,
                           @Value("${ghlove.point-service.base-url}") String pointBaseUrl,
                           @Value("${ghlove.order-service.base-url}") String orderBaseUrl,
                           @Value("${ghlove.gift-service.base-url}") String giftBaseUrl,
                           @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        this.member = build(memberBaseUrl, adminSecret);
        this.donation = build(donationBaseUrl, adminSecret);
        this.point = build(pointBaseUrl, adminSecret);
        this.order = build(orderBaseUrl, adminSecret);
        this.gift = build(giftBaseUrl, adminSecret);
    }

    private static RestClient build(String baseUrl, String secret) {
        return RestClient.builder().baseUrl(baseUrl).defaultHeader("X-Internal-Secret", secret).build();
    }

    /** AS-IS OpmanagerMainCount / OpmanagerCount (id·label·count). */
    public record CountRow(String id, String label, long count) {
    }

    /** AS-IS MainChart (cntrDay·cntrAmt). */
    public record ChartRow(String cntrDay, long cntrAmt) {
    }

    private static final ParameterizedTypeReference<List<CountRow>> COUNT_ROWS = new ParameterizedTypeReference<>() {
    };
    private static final ParameterizedTypeReference<List<ChartRow>> CHART_ROWS = new ParameterizedTypeReference<>() {
    };
    private static final ParameterizedTypeReference<Map<String, Long>> LONG_MAP = new ParameterizedTypeReference<>() {
    };

    // ---- member ----

    public Map<String, Long> subscribers() {
        return member.get().uri("/api/admin/members/dashboard/subscribers").retrieve().body(LONG_MAP);
    }

    public Map<String, Long> notificationAgree() {
        return member.get().uri("/api/admin/members/dashboard/notification-agree").retrieve().body(LONG_MAP);
    }

    // ---- donation ----

    public List<CountRow> donationMainInfo(String userRole, String locgovCd, String bankCode, String bankNm) {
        return donation.get().uri(u -> u.path("/api/admin/dashboard/main-info").queryParam("userRole", userRole)
                        .queryParam("locgovCd", nz(locgovCd)).queryParam("bankCode", nz(bankCode))
                        .queryParam("bankNm", nz(bankNm)).build())
                .retrieve().body(COUNT_ROWS);
    }

    public List<ChartRow> amountChart(String userRole, String locgovCd) {
        return chart("/api/admin/dashboard/amount-chart", userRole, locgovCd);
    }

    public List<ChartRow> countChart(String userRole, String locgovCd) {
        return chart("/api/admin/dashboard/count-chart", userRole, locgovCd);
    }

    public List<CountRow> chartInfo(String userRole, String locgovCd) {
        return donation.get().uri(u -> u.path("/api/admin/dashboard/chart-info").queryParam("userRole", userRole)
                .queryParam("locgovCd", nz(locgovCd)).build()).retrieve().body(COUNT_ROWS);
    }

    public List<CountRow> donationTableInfo(String date) {
        return donation.get().uri(u -> u.path("/api/admin/dashboard/table-info").queryParam("date", date).build())
                .retrieve().body(COUNT_ROWS);
    }

    private List<ChartRow> chart(String path, String userRole, String locgovCd) {
        return donation.get().uri(u -> u.path(path).queryParam("userRole", userRole)
                .queryParam("locgovCd", nz(locgovCd)).build()).retrieve().body(CHART_ROWS);
    }

    // ---- point ----

    public List<CountRow> pointMainInfo(String userRole, String locgovCd) {
        return point.get().uri(u -> u.path("/api/admin/dashboard/main-info").queryParam("userRole", userRole)
                .queryParam("locgovCd", nz(locgovCd)).build()).retrieve().body(COUNT_ROWS);
    }

    public List<CountRow> pointTableInfo(String date) {
        return point.get().uri(u -> u.path("/api/admin/dashboard/table-info").queryParam("date", date).build())
                .retrieve().body(COUNT_ROWS);
    }

    // ---- order ----

    /** {thisYear, total}. */
    public Map<String, Long> presentCount(String userRole, String locgovCd) {
        return order.get().uri(u -> u.path("/api/admin/orders/stats/dashboard-present").queryParam("userRole", userRole)
                .queryParam("locgovCd", nz(locgovCd)).build()).retrieve().body(LONG_MAP);
    }

    // ---- gift ----

    public Long giftItemCount(String date) {
        return gift.get().uri(u -> u.path("/api/admin/gifts/dashboard/item-count").queryParam("date", date).build())
                .retrieve().body(Long.class);
    }

    public List<String> giftItemLocgovCodes() {
        return gift.get().uri("/api/admin/gifts/dashboard/item-locgov-codes").retrieve()
                .body(new ParameterizedTypeReference<List<String>>() {
                });
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
