package com.ghlove.admin.web.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** AS-IS DefaultDataMasking(NAME·PHONE_NUMBER·ADDRESS_DETAIL·BIRTHDAY)과 결과가 같은지 - 전체주문관리 목록이 이 값을 그대로 보여준다. */
class DataMaskingTest {

    @Test
    void nameKeepsFirstAndLast() {
        assertThat(DataMasking.name("홍길동")).isEqualTo("홍*동");
        assertThat(DataMasking.name("남궁민수")).isEqualTo("남**수");
    }

    @Test
    void twoLetterNameKeepsOnlyFirst() {
        assertThat(DataMasking.name("홍길")).isEqualTo("홍*");
    }

    @Test
    void oneLetterAndNullAreUnchanged() {
        assertThat(DataMasking.name("김")).isEqualTo("김");
        assertThat(DataMasking.name(null)).isNull();
    }

    @Test
    void phoneMasksMiddleGroupAndKeepsHyphens() {
        assertThat(DataMasking.phoneNumber("010-1234-5678")).isEqualTo("010-****-5678");
        assertThat(DataMasking.phoneNumber("01012345678")).isEqualTo("010****5678");
        assertThat(DataMasking.phoneNumber("02-123-4567")).isEqualTo("02-***-4567");
    }

    @Test
    void phoneInOtherFormatIsUnchanged() {
        assertThat(DataMasking.phoneNumber("1234")).isEqualTo("1234");
    }

    @Test
    void addressDetailReplacesEachWordWithStarsAndKeepsTrailingSpace() {
        // AS-IS: 낱말마다 (글자수-1)개의 *, 낱말 뒤에 공백(마지막 낱말 뒤에도)
        assertThat(DataMasking.addressDetail("101동 202호")).isEqualTo("*** *** ");
        assertThat(DataMasking.addressDetail("44")).isEqualTo("* ");
    }

    @Test
    void emailKeepsFirstTwoLettersAndDomain() {
        // AS-IS Masking.EMAIL: 앞 두 글자 + 가운데 * + @ + 도메인
        assertThat(DataMasking.email("abcdef@test.com")).isEqualTo("ab****@test.com");
        assertThat(DataMasking.email("ab@test.com")).isEqualTo("ab@test.com");
    }

    @Test
    void emailInOtherFormatIsUnchanged() {
        assertThat(DataMasking.email("a@b.c")).isEqualTo("a@b.c");
        assertThat(DataMasking.email("no-at-sign")).isEqualTo("no-at-sign");
        assertThat(DataMasking.email(null)).isNull();
    }

    @Test
    void birthdayMasksThirdAndFourthCharacter() {
        assertThat(DataMasking.birthday("19800101")).isEqualTo("19**0101");
        assertThat(DataMasking.birthday("1980-01-01")).isEqualTo("19**-01-01");
        assertThat(DataMasking.birthday("198001")).isEqualTo("198001");
    }
}
