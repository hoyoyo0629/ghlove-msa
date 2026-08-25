package com.ghlove.admin.service.integration;

import java.math.BigDecimal;

/** NH농협으로 반출할 기부자 데이터 1건 (AS-IS NhApiBatchServiceImpl.setUserCntrForNh 대상 행). */
public record NhExportRow(String cntrSn, Long userId, String locgovCode, BigDecimal amount) {
}
