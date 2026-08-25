package com.ghlove.donation.service;

import java.math.BigDecimal;

/** 특정사업 상세화면 "응원메시지(기부내역)" 탭 표시용 - AS-IS cntrList처럼 이름/아이디
 *  둘 다 마스킹해서 노출한다(실명·실제 아이디는 노출하지 않음). */
public record CheerMessage(String maskedUserName, String maskedLoginId, String cntrDeFormatted, BigDecimal cntrAmt, String cheerMsg) {
}
