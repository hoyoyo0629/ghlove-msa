package com.ghlove.donation.service.integration;

import java.math.BigDecimal;

/** 국세청 홈택스 전자기부금영수증 등록 요청 (AS-IS sendNtsEreceipt 요청 파라미터에 해당). */
public record NtsReceiptRequest(String cntrSn, String donorName, String donorBirthday,
                                 String locgovBizrno, BigDecimal cntrAmt, String cntrDe) {
}
