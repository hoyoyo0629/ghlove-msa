package com.ghlove.admin.web;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 처리 완료 알림용 리다이렉트 - AS-IS가 $s.alert("...되었습니다")로 알리던 자리다.
 * 실패 경로의 ?errorMessage=와 같은 관행으로 문구를 ?done=에 실어 보내면, 화면에 붙은
 * fragments/alert :: gh-alert 모달이 그대로 띄운다(AS-IS #op-alert 재현).
 */
final class Done {

    private Done() {
    }

    static String redirect(String path, String message) {
        return "redirect:" + path + (path.contains("?") ? "&" : "?") + "done="
                + URLEncoder.encode(message, StandardCharsets.UTF_8);
    }
}
