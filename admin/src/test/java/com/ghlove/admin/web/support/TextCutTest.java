package com.ghlove.admin.web.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TextCutTest {

    private final TextCut textCut = new TextCut();

    @Test
    void returnsUnchangedWhenWithinLimit() {
        assertThat(textCut.cut("짧은 문자열", 20)).isEqualTo("짧은 문자열");
    }

    @Test
    void cutsAndAppendsEllipsisWhenOverLimit() {
        String value = "01234567890123456789extra";
        assertThat(textCut.cut(value, 20)).isEqualTo("01234567890123456789...");
    }

    @Test
    void nullReturnsEmptyString() {
        assertThat(textCut.cut(null, 20)).isEmpty();
    }
}
