package com.ghlove.admin.service;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 문자 구분 - AS-IS saleson.common.enumeration.SmsType을 그대로 옮긴 것(code=SVC_ID, title, description).
 * 문자전송이력(7208) 화면의 "문자 구분" 셀렉트가 code를 값으로, description을 라벨로 쓰고,
 * 목록의 "구분" 컬럼은 SVC_ID를 이 표로 되돌려 보여준다.
 */
public enum SmsType {

    JOIN_MEMBERSHIP("812-A001", "회원가입시", "1. 회원가입 안내(회원가입자)"),
    DONATION("812-A002", "기부시", "2. 기부감사 인사 메시지(기부자)"),
    TAX_CREDIT("812-A003", "세액 공제 알림", "3. 세액 공제 알림(기부자)"),
    PRESENT_ORDER("812-A004", "답례품 주문시", "4. 답례품 주문(답례품 신청자/답례품 제공자)"),
    PRESENT_SHIPPING("812-A005", "답례품 발송시 (배송준비배송중)", "5. 답례품 배송 안내(답례품 신청자)"),
    ILOVEGOHYANG_DAY("812-A006", "고향사랑의 날 알림", "6. 고향사랑의 날 알림(고향사랑e음 회원)"),
    OVERPAYMENT("812-A007", "과오납 처리시", "7. 과오납 처리(과오납자)"),
    OVERPAYMENT_CANCEL("812-A008", "과오납 취소시", "8. 과오납 취소(과오납자)"),
    PRESENT_CANCEL_REGISTER("812-A009", "답례품 취소 접수시", "9. 답례품 주문 취소 접수(답례품 신청자/답례품 제공자)"),
    PRESENT_CANCEL_COMPLETE("812-A010", "답례품 취소 완료시", "10. 답례품 주문 취소 완료(답례품 신청자)"),
    PRESENT_REFUND_REGISTER("812-A011", "답례품 환불/교환 접수", "11. 답례품 환불/교환 접수(답례품 신청자/답례품 제공자)"),
    PRESENT_REFUND_COMPLETE("812-A012", "답례품 환불/교환 완료시 (거절/수용)", "12. 답례품 환불/교환 완료(답례품 신청자)"),
    HONOR_DONATION("812-A013", "명예기부자 선정시", "13. 명예기부자 선정(일정금액 이상 기부자/지자체 선정)"),
    SECESSION("812-A014", "탈퇴시", "14. 회원탈퇴 안내 메시지(고향사랑e음 회원)"),
    ITEM_APPROVAL_REGISTER("812-A015", "상품등록 승인요청시", "15. 답례품 등록 요청(고향사랑e음 관리자)"),
    ITEM_APPROVAL_COMPLETE("812-A016", "상품등록 승인시", "16. 답례품 등록 승인(답례품 제공업체)"),
    ITEM_APPROVAL_CANCEL("812-A017", "상품등록 승인거절시", "17. 답례품 등록 승인 거절(답례품 제공업체)"),
    COMPANY_REGISTER("812-A018", "업체등록 완료시", "18. 답례품 제공자 등록(답례품 제공업체)"),
    CALCULATE_CHECK("812-A019", "정산예정내역 (정산확정)", "19. 답례품 정산예정(답례품 제공업체)"),
    CALCULATE_CONFIRM("812-A020", "정산확정내역 (정산확인)", "20. 답례품 정산확정(답례품 제공업체)"),
    CALCULATE_CLOSE("812-A021", "정산마감내역 (지급완료)", "21. 답례품 정산완료(답례품 제공업체)"),
    QNA("812-A022", "1:1 QnA 응답시", "22. 질의 응답(고향사랑e음 회원)"),
    PASSWORD_CHANGE_COMPLETE("812-A023", "비밀번호 변경 완료시", "23. 비밀번호 변경 메시지(고향사랑e음 회원)"),
    MANAGER_LOGIN("812-A024", "관리자 로그인", "24. 관리자 로그인(고향사랑e음 관리자)");

    private final String code;
    private final String title;
    private final String description;

    SmsType(String code, String title, String description) {
        this.code = code;
        this.title = title;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    /** SVC_ID → 구분명(title). 모르는 코드는 코드를 그대로 보여준다. */
    public static String titleOf(String code) {
        return Arrays.stream(values())
                .filter(t -> t.code.equals(code))
                .map(SmsType::getTitle)
                .findFirst()
                .orElse(code == null ? "" : code);
    }

    /** SVC_ID → enum. AS-IS {@code TifIpsSndngMDisplay.getSmsType} - 못 찾으면 null. */
    public static SmsType fromCode(String code) {
        return Arrays.stream(values())
                .filter(t -> t.code.equals(code))
                .findFirst()
                .orElse(null);
    }

    /** 화면 셀렉트용 - code → description(순서 유지). */
    public static Map<String, String> options() {
        Map<String, String> options = new LinkedHashMap<>();
        for (SmsType type : values()) {
            options.put(type.code, type.description);
        }
        return options;
    }
}
