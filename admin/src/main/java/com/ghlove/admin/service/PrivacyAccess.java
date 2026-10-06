package com.ghlove.admin.service;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.Arrays;

/**
 * 개인정보 접근 URL 레지스트리 (AS-IS saleson.common.enumeration.PrivacyAccess 이식).
 *
 * AS-IS는 이 표에 등록된 URL로 접근할 때만 개인정보 접근로그({@code OP_PRIVACY_ACCESS_LOG})를
 * 남기고, 엑셀 다운로드는 사유를 받는 공통 엔드포인트에서 {@link #findByUrl}로 이 표를 찾아
 * 요청명(NAME)과 수행업무(TASK)를 결정한다. 등록되지 않은 URL이면 AS-IS는 저장을 거부한다
 * ("개인정보 접근 URL이 아닙니다. 설정파일을 확인해주세요.").
 *
 * <b>상수 목록·요청명·수행업무·AS-IS 패턴은 한 글자도 바꾸지 않고 그대로 옮겼다.</b> AS-IS 패턴은
 * {@code /**}로 시작해 {@code /opmanager/...}·{@code /seller/...} 양쪽을 받는 모양인데 TO-BE의
 * 라우트는 다르므로, 이식이 끝난 화면에만 {@code tobePattern}을 덧붙였다(AS-IS 패턴도 그대로
 * 남겨 두어 남은 화면을 이식할 때 바로 붙일 수 있게 한다).
 */
public enum PrivacyAccess {
    // 회원관리
    USER_LIST(PrivacyTask.LIST, PrivacyTask.LIST,"/**/user/customer/list", "회원리스트"),
    SECEDE_USER_LIST(PrivacyTask.LIST, PrivacyTask.LIST, "/**/user/secede-user/list", "탈퇴회원 리스트"),

    USER_VIEW(PrivacyTask.VIEW, PrivacyTask.VIEW, "/**/user/popup/details/{userId}", "회원정보 요약보기"),
    USER_EDIT_VIEW(PrivacyTask.VIEW, PrivacyTask.UPDATE, "/**/user/popup/edit/{userId}", "회원정보 수정"),
    USER_SNS_VIEW(PrivacyTask.VIEW, PrivacyTask.VIEW, "/**/user/popup/sns-user/{userId}", "회원 SNS 연결상태"),
    USER_DELIVERY_VIEW(PrivacyTask.VIEW, PrivacyTask.VIEW, "/**/user/popup/delivery-list/{userId}", "회원 배송지 관리"),
    USER_POINT_VIEW(PrivacyTask.VIEW, PrivacyTask.VIEW, "/**/user/popup/point/point/{userId}", "회원 포인트 관리"),
    USER_SHIPPING_COUPON_VIEW(PrivacyTask.VIEW, PrivacyTask.VIEW, "/**/user/popup/point/shipping/{userId}", "회원 배송비 쿠폰관리"),
    USER_COUPON_VIEW(PrivacyTask.VIEW, PrivacyTask.VIEW, "/**/user/popup/coupon/{userId}", "회원 쿠폰조회"),
    USER_CLAIM_MEMO_VIEW(PrivacyTask.VIEW, PrivacyTask.VIEW, "/**/user/popup/claim-memo-list/{userId}", "회원 상담내역"),
    USER_SEND_MAIL_LOG_VIEW(PrivacyTask.VIEW, PrivacyTask.VIEW, "/**/user/popup/send-mail-log-list/{userId}", "회원 메일 발송내역"),
    USER_SEND_SMS_LOG_VIEW(PrivacyTask.VIEW, PrivacyTask.VIEW, "/**/user/popup/send-sms-log-list/{userId}", "회원 SMS 발송내역"),
    USER_SECEDE_VIEW(PrivacyTask.VIEW, PrivacyTask.DELETE, "/**/user/popup/delete/{userId}", "회원탈퇴"),

    USER_EXCEL_DOWNLOAD(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/user/customer/list/download-excel", "회원 엑셀 다운로드"),

    // 주문 관리
    ORDER_LIST(PrivacyTask.LIST, PrivacyTask.LIST, "/**/order/list", "주문전체 리스트"),
    WAITING_DEPOSIT_LIST(PrivacyTask.LIST, PrivacyTask.LIST, "/**/order/waiting-deposit", "입금대기 주문 목록"),
    NEW_ORDER_LIST(PrivacyTask.LIST, PrivacyTask.LIST, "/**/order/new-order", "신규주문 목록"),
    SHIPPING_READY_LIST(PrivacyTask.LIST, PrivacyTask.LIST, "/**/order/shipping-ready", "배송준비중 주문 목록"),
    SHIPPING_LIST(PrivacyTask.LIST, PrivacyTask.LIST, "/**/order/shipping", "배송중 주문 목록"),
    CONFIRM_LIST(PrivacyTask.LIST, PrivacyTask.LIST, "/**/order/confirm", "구매확정 주문 목록"),

    ORDER_VIEW(PrivacyTask.VIEW, PrivacyTask.VIEW, "/**/order/**/order-detail/{orderSequence}/{orderCode}", "주문내역 상세"),

    ORDER_EXCEL_DOWNLOAD(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/order/excel-download/**", "주문전체 엑셀 다운로드"),
    WAITING_DEPOSIT_EXCEL_DOWNLOAD(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/order/waiting-deposit/order-excel-download", "입금대기 주문 엑셀 다운로드"),
    NEW_ORDER_EXCEL_DOWNLOAD(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/order/new-order/order-excel-download", "신규주문 엑셀 다운로드"),
    SHIPPING_READY_EXCEL_DOWNLOAD(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/order/shipping-ready/order-excel-download", "배송준비중 주문 엑셀 다운로드"),
    SHIPPING_READY_MOBILE_EXCEL_DOWNLOAD(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/order/shipping-ready-mobile/order-excel-download", "배송준비중(모바일) 주문 엑셀 다운로드"),
    SHIPPING_EXCEL_DOWNLOAD(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/order/shipping/order-excel-download", "배송중 주문 엑셀 다운로드"),
    FINISH_EXCEL_DOWNLOAD(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/order/finish/order-excel-download", "배송완료 주문 엑셀 다운로드"),
    CONFIRM_EXCEL_DOWNLOAD(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/order/confirm/order-excel-download", "구매확정 주문 엑셀 다운로드"),
    ALL_LIST_EXCEL_DOWNLOAD(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/order/list/order-excel-download", "전체 주문 엑셀 다운로드", "/admin/orders/export"),

    // 주문 클레임 관리
    CANCEL_LIST(PrivacyTask.LIST, PrivacyTask.LIST, "/**/order/cancel/list", "주문취소 목록"),
    RETURN_LIST(PrivacyTask.LIST, PrivacyTask.LIST, "/**/order/return/list", "반품 목록"),
    EXCHANGE_LIST(PrivacyTask.LIST, PrivacyTask.LIST, "/**/order/exchange/list", "교환 목록"),
    REFUND_LIST(PrivacyTask.LIST, PrivacyTask.LIST, "/**/order/refund/list", "환불내역 목록"),

    REFUND_VIEW(PrivacyTask.VIEW, PrivacyTask.VIEW, "/**/order/refund/detail/{refundCode}", "환불내역 상세"),

    //오프라인 담당자
    OFF_CHARGER(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/user/off-charger/list/download-excel", "오프라인 담당자 목록", "/admin/off-person-in-charge/list/download-excel"),

    //기부금 관리
    GIVE_STATE(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/give/give-state/list/download-excel", "기부금 모금현황 목록"),
    GIVE_STATE_LOCGOV(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/give/give-state/list/locgov/download-excel", "기부금 모금현황 목록"),
    GIVE_STATE_VIEW(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/give/give-state/detail/download-excel", "기부금 모금현황 상세 목록"),
    GIVE_POINT(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/give/give-point/list/download-excel", "기부 포인트 목록"),
    GIVE_POINT_LOCGOV(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/give/give-point/list/locgov/download-excel", "기부 포인트 목록"),
    GIVE_POINT_VIEW(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/give/give-point/detail/download-excel", "기부 포인트 상세 목록"),
    GIVE_STATE_DETAIL(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/give/give-state/detail_list/download-excel", "기부금 전체현황 목록"),
    GIVE_OPERATION(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/give/give-operation/list/excel", "기부금 운용현황 목록"),
    GIVE_OPERATION_YEAR(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/give/give-operation/all/download-excel", "기부금 운용현황 목록"),
    GIVE_OPERATION_DETAIL(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/give/give-operation/all/download-excel", "기부금 운용현황 목록"),
    GIVE_LOCGOV_OPERATION(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/give/give-operation/list/**/excel", "기부금 지자체 운용현황 목록"),
    GIVE_STATISTICS_OPERATION(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/give/statistics/operate/excel", "기부금 운영현황 목록"),
    GIVE_STATISTICS_LIKE(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/statistics/locgov/like/excel", "관심지자체 목록"),

    //통계
    GIVE_STATISTICS_LOCGOV(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/give/statistics/locgov/excel", "기부금 지자체별 목록"),
    GIVE_STATISTICS_PERSON(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/give/statistics/person/excel", "기부금 인원별 목록"),
    GIVE_STATISTICS_PERSON_DETAIL(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/give/statistics/person/**/excel", "기부금 인원별 목록"),
    GIVE_STATISTICS_NUMBER(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/give/statistics/number/excel", "기부 건수 목록"),
    GIVE_STATISTICS_NUMBER_DETAIL(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/give/statistics/number/**/excel", "기부 건수 상세 목록"),
    GIVE_STATISTICS_DATE(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/give/statistics/date/excel", "기부 일자별 목록"),
    GIVE_STATISTICS_DATE_DETAIL(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/give/statistics/date/**/excel", "기부 일자별 상세 목록"),
    GIVE_STATISTICS_AMOUNT(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/give/statistics/amount/excel", "기부 금액 목록"),
    GIVE_STATISTICS_AMOUNT_DETAIL(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/give/statistics/amount/**/excel", "기부 금액 상세 목록"),

//    SHOP_STATISTICS_SALES(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/shop-statistics/sales/locgov/mois/download-excel", "답례품 구매현황 목록"),
//    SHOP_STATISTICS_LOCGOV_SALES(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/shop-statistics/sales/locgov/download-excel", "답례품 구매현황 지자체별 목록"),
//    SHOP_STATISTICS_WISH(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/shop-statistics/wish/list/mois/download-excel", "답례품 선호도 목록"),
//    SHOP_STATISTICS_LOCGOV_WISH(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/shop-statistics/wish/list/locgov/download-excel", "답례품 선호도 지자체별 목록"),

    //오프라인 기부
    OFF_GIVE(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/offgive/list/download-excel", "오프라인 기부금 목록"),
    OFF_GIVE_PRJ(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/offgive/prj/list/download-excel", "오프라인 기부금 목록-특정사업기부"),

    // 정산
    REMITTANCE_EXPECTED_EXCEL_DOWNLOAD(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/remittance/expected/list-excel", "정산 예정 리스트 엑셀 다운로드"),
    REMITTANCE_EXPECTED_DETAIL_EXCEL_DOWNLOAD(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/remittance/expected/detail/item-excel", "정산 예정 상세 리스트 엑셀 다운로드"),
    REMITTANCE_CONFIRM_EXCEL_DOWNLOAD(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/remittance/confirm/list-excel", "정산 확정 리스트 엑셀 다운로드"),
    REMITTANCE_CONFIRM_DETAIL_EXCEL_DOWNLOAD(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/remittance/confirm/detailNew-excel/**/**", "정산 확정 상세 리스트 엑셀 다운로드"),
    REMITTANCE_FINISH_EXCEL_DOWNLOAD(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/remittance/finish/list-excel", "정산 마감 리스트 엑셀 다운로드"),
    REMITTANCE_FINISH_DETAIL_EXCEL_DOWNLOAD(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/remittance/finish/detail-excel/**/**", "정산 마감 상세 리스트 엑셀 다운로드"),
    REMITTANCE_TEMP_DETAIL_EXCEL_DOWNLOAD(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/temp-process/remittance/view-excel/**", "정산 미정 상세 리스트 엑셀 다운로드"),

    // 답례품
    ITEM_REVIEW_EXCEL_DOWNLOAD(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/item/review/download-excel", "답례품 후기 리스트 엑셀 다운로드", "/admin/gift-reviews/export"),
    ITEM_QNA_EXCEL_DOWNLOAD(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/qna-item/download-excel", "답례품 QnA 리스트 엑셀 다운로드"),

    // 고객센터
    MAINTENANCE_EXCEL_DOWNLOAD(PrivacyTask.EXCEL_DOWNLOAD, PrivacyTask.EXCEL_DOWNLOAD, "/**/maintenance/list/mainten-excel-download", "운영관리 SR게시판", "/admin/maintenance/export")

    ;

    private final PrivacyTask task;
    private final PrivacyTask actionTask;
    private final String pattern;
    private final String name;
    /** TO-BE 라우트 패턴 - AS-IS에는 없는 필드다(위 클래스 주석 참고). 아직 없으면 null. */
    private final String tobePattern;

    PrivacyAccess(PrivacyTask task, PrivacyTask actionTask, String pattern, String name) {
        this(task, actionTask, pattern, name, null);
    }

    PrivacyAccess(PrivacyTask task, PrivacyTask actionTask, String pattern, String name, String tobePattern) {
        this.task = task;
        this.actionTask = actionTask;
        this.pattern = pattern;
        this.name = name;
        this.tobePattern = tobePattern;
    }

    public PrivacyTask getTask() {
        return task;
    }

    public PrivacyTask getActionTask() {
        return actionTask;
    }

    public String getPattern() {
        return pattern;
    }

    public String getName() {
        return name;
    }

    public String getTobePattern() {
        return tobePattern;
    }

    /** AS-IS findByUrl - 패턴이 맞는 첫 항목. TO-BE 라우트도 함께 본다. */
    public static PrivacyAccess findByUrl(PathMatcher antPathMatcher, String url) {
        if (url == null) {
            return null;
        }
        return Arrays.stream(PrivacyAccess.values())
                .filter(privacyAccess -> antPathMatcher.match(privacyAccess.getPattern(), url)
                        || (privacyAccess.getTobePattern() != null
                            && antPathMatcher.match(privacyAccess.getTobePattern(), url)))
                .findAny()
                .orElse(null);
    }

    public static PrivacyAccess findByUrl(String url) {
        return findByUrl(new AntPathMatcher(), url);
    }
}
