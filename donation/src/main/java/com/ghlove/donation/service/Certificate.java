package com.ghlove.donation.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/** 기부확인증 (AS-IS getReceiptPopInfo 응답에 해당). */
@Getter
@AllArgsConstructor
public class Certificate {
    private final String userName;
    private final String birthdayDisplay;
    private final String topLocGovDisplay;
    private final BigDecimal totalCntrAmt;
    private final int totalCnt;
    private final String nowDateDisplay;
    private final List<ReceiptDetailRow> rows;
    /** topLocGov의 직인 이미지 data URI - 등록된 직인이 없으면 null. */
    private final String sealImageDataUri;
}
