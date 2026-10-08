package com.ghlove.admin.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
class SmsIpsServiceTest {

    @Autowired
    private SmsIpsService smsIpsService;

    @Test
    void searchAllowsNullDateRange() {
        assertThatCode(() -> smsIpsService.search(null, null, null, null))
                .doesNotThrowAnyException();
    }

    @Test
    void donationLimitAmtDetailReadsSeededCommonCode() {
        // migration-admin-common-code-asis-load.sql이 2024~2030년 DONATION_LIMIT_AMT를
        // DETAIL='2천만'으로 적재해 둔 범위 안이라면 그대로 읽혀야 한다.
        assertThat(smsIpsService.donationLimitAmtDetail()).isEqualTo("2천만");
    }
}
