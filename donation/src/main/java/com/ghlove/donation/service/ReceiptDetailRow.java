package com.ghlove.donation.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/** 기부확인증 뒷면(고향사랑기부내역)의 한 행 - AS-IS getReceiptPopListInfo 결과 1건. */
@Getter
@AllArgsConstructor
public class ReceiptDetailRow {
    private final String cntrSn;
    private final String cntrDeDisplay;
    private final String upperLocgovNm;
    private final String locgovNm;
    private final BigDecimal cntrAmt;
    private final String bizRno;
    /** "Y"/"N" - 특별재난지역 기부 여부. */
    private final String spelDstrYn;
}
