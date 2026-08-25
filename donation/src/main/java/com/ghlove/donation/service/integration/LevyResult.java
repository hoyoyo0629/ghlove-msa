package com.ghlove.donation.service.integration;

import java.time.LocalDateTime;

/** 세외수입 부과 등록 결과 (AS-IS sntrBugaInsert/contryBugaInsert 응답에 해당). */
public record LevyResult(String bugaNo, LocalDateTime bugaDate) {
    public static LevyResult mock(String cntrSn) {
        return new LevyResult("MOCK-BUGA-" + cntrSn, LocalDateTime.now());
    }
}
