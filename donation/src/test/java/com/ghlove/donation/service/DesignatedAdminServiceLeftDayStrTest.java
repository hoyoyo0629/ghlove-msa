package com.ghlove.donation.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AS-IS {@code DesignatedDonation.getLeftDayStr()} 재현 확인.
 *
 * <p>AS-IS의 {@code leftDay} 는 매퍼의 {@code DATEDIFF(NOW(), 종료일)} 이라 <b>오늘 - 종료일</b>
 * 이다. 종료일이 남아 있으면 음수가 되고, 그 값에 "D"를 붙이는 것이 D-87 의 정체다.
 * 화면에서는 당일(D-Day) 경우를 만들기 어려워 여기서 세 분기를 모두 묶어 둔다.
 */
class DesignatedAdminServiceLeftDayStrTest {

    @Test
    @DisplayName("종료일이 오늘이면 D-Day")
    void today() {
        assertThat(DesignatedAdminService.leftDayStr("20261013", "20261013")).isEqualTo("D-Day");
    }

    @Test
    @DisplayName("종료일이 남아 있으면 D-n (AS-IS가 음수를 그대로 이어 붙인 결과)")
    void remaining() {
        assertThat(DesignatedAdminService.leftDayStr("20261231", "20261013")).isEqualTo("D-79");
        assertThat(DesignatedAdminService.leftDayStr("20261014", "20261013")).isEqualTo("D-1");
    }

    @Test
    @DisplayName("종료일이 지났으면 D+n")
    void passed() {
        assertThat(DesignatedAdminService.leftDayStr("20250831", "20261013")).isEqualTo("D+408");
        assertThat(DesignatedAdminService.leftDayStr("20261012", "20261013")).isEqualTo("D+1");
    }

    @Test
    @DisplayName("종료일이 없거나 형식이 깨졌으면 D-Day 로 둔다(화면이 깨지지 않게)")
    void malformed() {
        assertThat(DesignatedAdminService.leftDayStr(null, "20261013")).isEqualTo("D-Day");
        assertThat(DesignatedAdminService.leftDayStr("2026", "20261013")).isEqualTo("D-Day");
        assertThat(DesignatedAdminService.leftDayStr("20261399", "20261013")).isEqualTo("D-Day");
    }
}
