package com.ghlove.admin.web;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * AS-IS {@code OpmanagerController.healthcheck}({@code GET /opmanager/healthcheck}) - L4·모니터링이 부르는
 * 생존확인. AS-IS {@code OpmanagerHandlerInterceptor.isManagerPage}가 로그인 검사에서 뺀 경로라 TO-BE도
 * 게이트 대상(WebConfig addPathPatterns)에 넣지 않는다. 응답은 text/plain "Server is alive." 그대로.
 *
 * <p>{@code GET /error/404}는 AS-IS {@code ErrorController.handle}이다 - 서버 파일관리(/file/index)가
 * 시스템 관리자가 아니면 여기로 보낸다. AS-IS 화면은 정적 public/error/404.html("페이지 오류")이고,
 * 같은 페이지를 이미 verbatim 복사해 둔 bix5-access/index를 404 상태로 쓴다.
 */
@Controller
public class HealthCheckController {

    @ResponseBody
    @GetMapping("/healthcheck")
    public String healthcheck() {
        return "Server is alive.";
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @GetMapping("/error/404")
    public String error404() {
        return "bix5-access/index";
    }
}
