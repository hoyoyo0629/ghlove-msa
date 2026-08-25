package com.ghlove.order.web;

import com.ghlove.order.service.JwtVerifier;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * SFR-010: fragments/header.html의 로그인/로그아웃 상태 표시가 GH_AUTH 쿠키를 보고
 * 판단할 수 있도록 모든 컨트롤러에 loggedIn 모델 속성을 공통으로 주입한다. 예전에는
 * 이 서비스에 로그인 상태를 알 방법이 없어(게이트웨이 세션 미전파) 헤더가 항상
 * 비로그인 상태로 고정 표시됐는데, 이제는 공유 JWT 쿠키로 실제 상태를 알 수 있다.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class HeaderAuthAdvice {

    private final JwtVerifier jwtVerifier;

    @ModelAttribute("loggedIn")
    public boolean loggedIn(HttpServletRequest request) {
        return jwtVerifier.currentUserId(request).isPresent();
    }
}
