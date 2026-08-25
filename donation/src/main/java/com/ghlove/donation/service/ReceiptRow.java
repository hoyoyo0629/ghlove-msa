package com.ghlove.donation.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/** 기부확인증 목록 화면(receiptList.html)의 한 행. */
@Getter
@AllArgsConstructor
public class ReceiptRow {
    private final String cntrSn;
    private final String cntrDeDisplay;
    private final String upperLocgovNm;
    private final String locgovNm;
    private final BigDecimal cntrAmt;
}
