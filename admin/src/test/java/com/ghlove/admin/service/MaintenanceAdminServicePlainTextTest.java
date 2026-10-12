package com.ghlove.admin.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 운영관리 SR게시판(5113) 목록의 '내용' 칸은 스마트에디터 HTML이 그대로 저장돼 있어
 * 표에 태그가 노출됐다(2026-10-12 사용자 지적). 목록·엑셀에서만 평문으로 바꾼다.
 */
class MaintenanceAdminServicePlainTextTest {

    @Test
    @DisplayName("에디터가 만든 <p> 태그를 벗긴다")
    void stripsParagraphTag() {
        assertEquals("xcvxcvxcvtesttesttesttesttest",
                MaintenanceAdminService.plainText("<p>xcvxcvxcvtesttesttesttesttest</p>"));
    }

    @Test
    @DisplayName("블록 끝과 <br>은 공백으로 바꿔 한 줄로 만든다")
    void blockEndsBecomeSpace() {
        assertEquals("첫줄 둘째줄", MaintenanceAdminService.plainText("<p>첫줄</p><p>둘째줄</p>"));
        assertEquals("첫줄 둘째줄", MaintenanceAdminService.plainText("첫줄<br>둘째줄"));
        assertEquals("첫줄 둘째줄", MaintenanceAdminService.plainText("첫줄\n둘째줄"));
    }

    @Test
    @DisplayName("HTML 엔티티를 되돌린다")
    void decodesEntities() {
        assertEquals("a < b & 'c'", MaintenanceAdminService.plainText("a &lt; b &amp; &#39;c&#39;"));
        assertEquals("빈 칸", MaintenanceAdminService.plainText("빈&nbsp;칸"));
    }

    @Test
    @DisplayName("속성이 붙은 태그와 중첩 태그도 벗긴다")
    void stripsTagsWithAttributes() {
        assertEquals("굵게",
                MaintenanceAdminService.plainText("<p style=\"color:red\"><strong>굵게</strong></p>"));
    }

    @Test
    @DisplayName("null·빈 값은 그대로 둔다")
    void keepsNullAndBlank() {
        assertNull(MaintenanceAdminService.plainText(null));
        assertEquals("", MaintenanceAdminService.plainText(""));
    }
}
