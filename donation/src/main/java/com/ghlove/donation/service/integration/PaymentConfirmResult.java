package com.ghlove.donation.service.integration;

import java.time.LocalDateTime;

/** 세외수입 수납 확인 결과 (AS-IS etaxSunapInfo/contrySunapInfo 응답에 해당). */
public record PaymentConfirmResult(String sunapYn, LocalDateTime sunapDate) {
    public static PaymentConfirmResult mock() {
        return new PaymentConfirmResult("Y", LocalDateTime.now());
    }
}
