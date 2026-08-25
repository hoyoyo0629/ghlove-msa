package com.ghlove.member.service;

import java.math.BigDecimal;

/** donation 서비스 GET /api/designated-projects 응답 매핑 (메인 화면 "특정사업에 기부하기"). */
public record DesignatedProjectInfo(Long dsgnDntnBizId, String title, String content, String locgovCode,
                                     String locgovName, Long goalAmt, BigDecimal raisedAmt, BigDecimal percent,
                                     String beginYmd, String endYmd, String imageUrl) {
}
