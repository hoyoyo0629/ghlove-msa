package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.Setter;

/**
 * 연계 로그 조회 검색조건 (메뉴 1413~1416) - AS-IS는 화면마다 DTO가 달랐지만
 * ({@code GifSeoulParam}, {@code NextBugaRequestLogDto}, {@code NextSunapRequestLogDto})
 * 필드가 겹치고 파라미터 이름이 같아 하나로 받는다. 화면별로 쓰는 필드만 다르다.
 *
 * <ul>
 *   <li>1413 서울부과: srchErrorCd(전체/SUCCESS/FAIL) · srchTxt(전자납부번호) · 등록일 범위</li>
 *   <li>1414 서울수납: srchRstCd(전체/SUCCESS/FAIL) · srchTxt(전자납부번호) · 등록일 범위</li>
 *   <li>1415 지방부과: bugaStatusCd(응답코드) · linkRstCd(연계결과코드) · epayNo(전자납부번호,
 *       연계결과메시지 LIKE) · srchpyrNm(납부자명) · srchlinkRstYn(전체/Y/N 라디오) · 부과일자 범위</li>
 *   <li>1416 지방수납: epayNo(전자납부번호) · 수납일자 범위</li>
 * </ul>
 */
@Getter
@Setter
public class LevyLogParam {

    /** 서울 두 화면의 검색어(전자납부번호) - AS-IS는 완전일치로 본다. */
    private String srchTxt;

    /** 1413 결과여부 - "" / SUCCESS / FAIL. */
    private String srchErrorCd;

    /** 1414 결과여부 - "" / SUCCESS / FAIL. */
    private String srchRstCd;

    /** 1415 응답코드. */
    private String bugaStatusCd;

    /** 1415 연계결과코드. */
    private String linkRstCd;

    /** 1415·1416 전자납부번호. */
    private String epayNo;

    /** 1415 납부자명 - AS-IS는 완전일치. */
    private String srchpyrNm;

    /** 1415 연계결과 라디오 - "" / Y(성공, link_rst_cd='000') / N(실패). */
    private String srchlinkRstYn;

    private String srchStartLogDate;

    private String srchEndLogDate;

    private int page = 1;

    /** AS-IS 기본 목록수 20 (화면출력 10/20/50/100). */
    private int itemsPerPage = 20;

    /** AS-IS가 "사용자가 화면출력을 바꿨는지"를 판정하는 보조 필드. */
    private int itemsPerPageTemp = 0;

    /** AS-IS 폼이 hidden으로 들고 다니는 값들(쿼리에는 쓰이지 않는다). */
    private String sort;
    private String orderBy;
    private String query;
}
