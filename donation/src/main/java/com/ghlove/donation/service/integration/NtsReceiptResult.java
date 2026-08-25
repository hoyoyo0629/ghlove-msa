package com.ghlove.donation.service.integration;

/** 국세청 전자기부금영수증 등록 결과. */
public record NtsReceiptResult(String receiptNo) {
    public static NtsReceiptResult mock(String cntrSn) {
        return new NtsReceiptResult("MOCK-NTS-" + cntrSn);
    }
}
