package com.ghlove.admin.web;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 기부현황 대시보드 (AS-IS opmanager 메뉴 '기부현황 대시보드', menu_url=/opmanager/bix5-access).
 *
 * AS-IS는 이 menu_url에 대응하는 서버 핸들러가 전혀 없어(BIX5 외부 BI 링크로만 존재) 클릭하면 404로
 * 떨어지고 ghlove-web public/error/404.html("페이지가 존재하지 않습니다")이 뜬다 - 즉 AS-IS에서 이
 * 기능은 "메뉴는 있으나 눌러보면 페이지 오류" 상태다. [[as-is-parity-includes-disabled-state]] 원칙대로
 * TO-BE도 메뉴만 노출하고 클릭 시 AS-IS와 동일한 404 페이지를 404 상태로 반환한다(verbatim 복사).
 * 실제 BI 연동/자체 대시보드 이식은 후속 라운드.
 */
@Controller
public class Bix5AccessController {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @GetMapping("/admin/bix5-access")
    public String index() {
        return "bix5-access/index";
    }
}
