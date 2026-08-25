package com.ghlove.donation.service.integration;

import java.math.BigDecimal;

/** 세외수입 부과 등록 요청 (AS-IS sntrBugaInsert/contryBugaInsert 요청 파라미터에 해당). */
public record BugaRequest(String cntrSn, Long userId, String cntrDe, String locgovCode, BigDecimal cntrAmt) {
}
