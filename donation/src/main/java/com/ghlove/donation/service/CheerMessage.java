package com.ghlove.donation.service;

import java.math.BigDecimal;

/** 특정사업 상세화면 "응원메시지(기부내역)" 탭 표시용 - AS-IS cntrList처럼 이름/아이디
 *  둘 다 마스킹해서 노출한다(실명·실제 아이디는 노출하지 않음).
 *  giveOrder=1이면 로그인한 본인의 기부건 → 인라인 입력+"적용"(saveCheerMsg) 노출, 0이면 읽기전용.
 *  cntrSn은 본인 기부건일 때만 채운다(남의 기부번호는 내려주지 않는다). */
public record CheerMessage(String cntrSn, int giveOrder, String maskedUserName, String maskedLoginId,
                           String cntrDeFormatted, BigDecimal cntrAmt, String cheerMsg) {
}
