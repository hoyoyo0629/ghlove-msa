package com.ghlove.admin.service;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 이메일 설정의 템플릿 목록과 메일 대체코드 - AS-IS saleson.shop.mailconfig.support.MailTemplate
 * 및 그 하위 *Mail 클래스들의 {@code getMap()}을 그대로 옮긴 것.
 *
 * <p>템플릿은 AS-IS {@code getTemplateCodes()}의 **고정 10종**이다(순서까지 동일). 예전 TO-BE는
 * 이 목록을 공통코드 ORDER_STATUS로 만들어 썼는데 그건 주문상태라 회원가입·임시비밀번호·문의답변·
 * 휴면안내·관리자 권한 승인/거절 템플릿이 아예 없었다.
 *
 * <p>대체코드는 각 메일 클래스의 {@code getMap()}(코드명 → 설명)을 AS-IS {@code getCodes()}와
 * 같은 규칙으로 {@code {under_score}} 패턴으로 바꿔 보여준다.
 */
@Component
public class MailTemplateCodes {

    /** AS-IS M00246 = "포인트" - expiration_point 라벨이 이 문구 + "소멸예정"이다. */
    private static final String POINT_LABEL = "포인트";

    private final Map<String, String> templates = new LinkedHashMap<>();
    private final Map<String, Map<String, String>> changeCodeMaps = new LinkedHashMap<>();

    public MailTemplateCodes() {
        templates.put("order_deposit_wait", "입금대기");
        templates.put("order_cready_payment", "결제완료");
        templates.put("order_delivering", "배송중");
        templates.put("expiration_point", POINT_LABEL + "소멸예정");
        templates.put("member_join", "회원가입");
        templates.put("pwsearch", "임시비밀번호 안내");
        templates.put("qna_complete", "문의답변");
        templates.put("member_sleep", "휴면안내");
        templates.put("manager_request_approval", "관리자 권한 승인");
        templates.put("manager_request_reject", "관리자 권한 거절");

        // AS-IS MailConfigServiceImpl.getMailChangeCodes 의 템플릿 → 메일클래스 매핑 그대로
        Map<String, String> orderMail = map(
                "orderName", "주문자명", "orderCode", "주문코드", "orderDate", "주문일자",
                "mobile", "휴대폰번호", "email", "이메일", "orderDetailList", "주문상품정보",
                "orderPrice", "주문금액", "usePoint", "할인금액", "deliveryPrice", "배송비",
                "orderTotalPrice", "총 결제금액", "approvalType", "결제방법",
                "bankInName", "입금자명", "bankDate", "입금기한", "orderItemPayment", "결제내역",
                "siteName", "상점명", "siteUrl", "상점URL", "bankAmount", "입금요청액",
                "bankVirtualNo", "가상계좌번호");
        changeCodeMaps.put("order_deposit_wait", orderMail);
        changeCodeMaps.put("order_cready_payment", orderMail);
        changeCodeMaps.put("order_delivering", map(
                "orderName", "주문자명", "orderCode", "주문코드", "orderDate", "주문일자",
                "siteName", "상점명", "siteUrl", "상점URL",
                "itemName10", "상품명(10자)", "itemName20", "상품명(20자)", "itemName30", "상품명(30자)",
                "delivery_number", "송장번호", "fullItemName", "상품명"));
        changeCodeMaps.put("pwsearch", map(
                "name", "이름", "compName", "회사명", "findId", "아이디", "findPw", "비밀번호",
                "siteName", "사이트명", "siteUrl", "사이트URL"));
        changeCodeMaps.put("member_join", map(
                "addr", "주소", "addrDetail", "상세주소", "email", "이메일", "emoney", "E-Money",
                "id", "아이디", "mileage", POINT_LABEL, "mobile", "핸드폰번호", "name", "이름",
                "phone", "전화번호", "regdate", "등록일자", "siteName", "상점명", "siteUrl", "상점URL",
                "zipcode", "우편번호"));
        changeCodeMaps.put("expiration_point", map(
                "email", "이메일", "id", "아이디", "name", "이름", "expirationDate", "소멸 일자",
                "expirationPoint", "소멸 포인트", "searchDate", "조회 일자", "siteName", "상점명"));
        changeCodeMaps.put("member_sleep", map(
                "after30", "30일 후 날짜", "after31", "31일 후 날짜", "siteName", "상점명",
                "siteUrl", "상점URL", "loginId", "사용자 아이디"));
        changeCodeMaps.put("qna_complete", map(
                "siteName", "상점명", "userName", "이름", "email", "이메일", "orderCode", "주문번호",
                "itemName", "상품명", "qnaGroup", "QNA-GROUP", "qnaType", "QNA-TYPE",
                "question", "문의내용", "subject", "문의제목", "created_date", "문의일자",
                "answer", "답변내용", "title", "답변제목", "answer_date", "답변일자",
                "siteUrl", "사이트 주소", "compName", "회사이름"));
        changeCodeMaps.put("manager_request_approval", map(
                "loginId", "아이디", "adminRole", "권한", "siteName", "상점명"));
        changeCodeMaps.put("manager_request_reject", map(
                "rejectResn", "거절사유"));
    }

    /** AS-IS getTemplateCodes - 화면 좌측 템플릿 목록(순서 고정). */
    public Map<String, String> templateCodes() {
        return templates;
    }

    /** AS-IS getTemplateCodeTitle - 없으면 빈 문자열. */
    public String templateCodeTitle(String templateId) {
        return templates.getOrDefault(templateId, "");
    }

    /** AS-IS getFirstTemplateCodeKey - 목록 진입 시 리다이렉트 대상. */
    public String firstTemplateCodeKey() {
        return templates.keySet().iterator().next();
    }

    public boolean exists(String templateId) {
        return templates.containsKey(templateId);
    }

    /**
     * AS-IS getMailChangeCodes + MailTemplate.getCodes - 코드명을 {under_score} 패턴으로 바꿔 준다.
     * 매핑이 없는 템플릿은 AS-IS도 빈 맵을 돌려준다(대체코드 팝업이 비어 보인다).
     */
    public Map<String, String> changeCodes(String templateId) {
        Map<String, String> source = changeCodeMaps.get(templateId);
        if (source == null) {
            return Map.of();
        }
        Map<String, String> codes = new LinkedHashMap<>();
        source.forEach((name, desc) -> codes.put("{" + toUnderScore(name) + "}", desc));
        return codes;
    }

    /** AS-IS StringUtils.convertToUnderScore - camelCase → camel_case. 이미 snake면 그대로. */
    private static String toUnderScore(String name) {
        StringBuilder sb = new StringBuilder();
        for (char c : name.toCharArray()) {
            if (Character.isUpperCase(c)) {
                sb.append('_').append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static Map<String, String> map(String... pairs) {
        Map<String, String> result = new LinkedHashMap<>();
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            result.put(pairs[i], pairs[i + 1]);
        }
        return result;
    }

    /** 템플릿 목록을 화면에서 쓰기 쉬운 형태로. */
    public List<Map.Entry<String, String>> templateEntries() {
        return List.copyOf(templates.entrySet());
    }
}
