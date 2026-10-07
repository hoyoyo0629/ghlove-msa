package com.ghlove.admin.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * 1411 엑셀다운로드사유관리 500 재발 방지 가드. onlyManagerId는 전체보기 권한(ROLE_ADMIN_1~4)일 때
 * null로 넘어오는데, {@code (:x is null or ...)} 꼴 JPQL은 cast를 붙여도 Hibernate가 null 값을
 * bytea로 바인딩해 PostgreSQL이 "cannot cast type bytea to bigint"로 거절했다(2026-10-07,
 * [[hql-null-param-needs-cast]]). 조건부 조립으로 바꾼 뒤에도 이 경로가 깨지지 않는지 확인한다.
 */
@SpringBootTest
class PrivacyAccessLogServiceTest {

    @Autowired
    private PrivacyAccessLogService privacyAccessLogService;

    @Test
    void excelDownloadLogsAllowsNullManagerId() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        assertThatCode(() -> privacyAccessLogService.excelDownloadLogs(today, today, null))
                .doesNotThrowAnyException();
    }
}
