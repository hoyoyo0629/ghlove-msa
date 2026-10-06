package com.ghlove.admin.web.support;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * AS-IS {@code ViewUtils.redirect(url, message)} 대응 - 리다이렉트하면서 안내 문구를
 * <b>1회용으로</b> 넘긴다.
 *
 * <p>AS-IS 바이트코드를 확인한 결과 이 메서드는 {@code FlashMapUtils.setMessage(message)} 뒤에
 * {@code redirect(url)}을 부른다. 즉 메시지는 <b>flash scope</b>로 가고 <b>URL은 깨끗하다</b>.
 * TO-BE는 그동안 {@code ?errorMessage=...}로 쿼리에 붙여서
 * {@code /popups?errorMessage=%EC%88%98%EC%A0%95...} 같은 주소가 남고, 새로고침하면 안내가
 * 다시 떴다(2026-10-06 사용자 지적).
 *
 * <p>AS-IS의 {@code FlashMapUtils}가 정적 메서드인 것처럼 호출부가 인자를 더 받지 않아도 되게
 * 요청 스코프 프록시({@link HttpServletRequest})를 주입받아 쓴다 - 컨트롤러 메서드 시그니처에
 * {@code RedirectAttributes}를 추가하지 않아도 된다.
 *
 * <p>화면에서는 {@code ${redirectMessage}}로 읽는다(기존 템플릿의 변수명과 같다).
 */
@Component
@RequiredArgsConstructor
public class FlashRedirect {

    /** 템플릿이 읽는 모델 키. */
    public static final String MESSAGE_KEY = "redirectMessage";

    private final HttpServletRequest request;

    /** @return {@code "redirect:" + url} - 컨트롤러가 그대로 반환하면 된다. */
    public String to(String url, String message) {
        if (message != null && !message.isBlank()) {
            RequestContextUtils.getOutputFlashMap(request).put(MESSAGE_KEY, message);
        }
        return "redirect:" + url;
    }
}
