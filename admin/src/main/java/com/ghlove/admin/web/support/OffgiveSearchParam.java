package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 기부금 접수관리(메뉴 15101) 검색조건 - AS-IS {@code saleson.shop.offgive.domain.Offgive}의
 * 검색 필드(sh* 접두어) 중 list.jsp가 보내는 것.
 *
 * <p>AS-IS 컨트롤러가 <b>신청일 범위를 비우면 오늘로 채운다</b>.
 */
@Getter
@Setter
public class OffgiveSearchParam {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** 검색구분 - CNTR_SN(접수번호)/LOCGOV_NM(기부 지자체)/ELCTRN_PAY_NO(전자납부번호)/RCEPT_BANK_NM(지점·센터명). */
    private String shKeyword;

    private String shText;

    /** 신청일 범위(yyyyMMdd) - 비면 오늘. */
    private String shFrstRegistPnttmStart;

    private String shFrstRegistPnttmEnd;

    /** 금액 범위. */
    private BigDecimal shCntrAmtStart;

    private BigDecimal shCntrAmtEnd;

    /** 기부상태 - ""(전체)/100(신고)/200(수납)/300(과오납). AS-IS 숫자코드를 그대로 쓴다. */
    private String shCntrSttusCode;

    /** 소속지점 코드(공통코드 OFF_BANK_LIST). 오프라인 담당자는 컨트롤러가 자기 지점으로 고정한다. */
    private String shRceptBankCode;

    /** 지점명 - 오프라인 부담당자·센터는 지점명까지 고정된다(AS-IS shRceptBankNm). */
    private String shRceptBankNm;

    /** AS-IS 폼이 hidden으로 들고 다니는 값. */
    private String query;

    private int page = 1;

    private int itemsPerPage = 10;

    /** AS-IS 컨트롤러 - 신청일이 비면 오늘로 채운다. */
    public void applyDefaults() {
        String today = LocalDate.now().format(DAY);
        if (shFrstRegistPnttmStart == null || shFrstRegistPnttmStart.isBlank()) {
            shFrstRegistPnttmStart = today;
        }
        if (shFrstRegistPnttmEnd == null || shFrstRegistPnttmEnd.isBlank()) {
            shFrstRegistPnttmEnd = today;
        }
        if (itemsPerPage <= 0) {
            itemsPerPage = Pagination.DEFAULT_ITEMS_PER_PAGE;
        }
    }
}
