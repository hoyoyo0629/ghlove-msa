package com.ghlove.admin.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 도로명주소 검색 팝업 (AS-IS saleson.shop.juso.JusoController {@code /opmanager/juso-popup}).
 *
 * <p>AS-IS에서 지자체관리(메뉴 4401)의 "주소찾기", 오프라인 기부접수 등 여러 화면이 공유하는
 * 공통 팝업이고, 행정안전부 주소검색 API({@code business.juso.go.kr})를 <b>브라우저에서 JSONP로
 * 직접</b> 호출한다 - 서버는 화면만 내려준다(AS-IS 컨트롤러도 뷰 반환 한 줄뿐이다).
 *
 * <p>AS-IS는 승인키·도메인·결과형식을 JSP 안에 상수로 박아 뒀는데, 운영에서 키를 바꿀 수 있어야
 * 하므로 설정({@code ghlove.juso.*})으로 뺐다. 기본값은 AS-IS 소스에 있던 값 그대로다.
 */
@Controller
public class JusoPopupController {

    @Value("${ghlove.juso.confm-key}")
    private String confmKey;

    @Value("${ghlove.juso.domain}")
    private String domain;

    /** 검색결과 화면 출력유형 - 1 도로명 / 2 +지번 / 3 +상세건물명 / 4 도로명+지번+상세건물명. */
    @Value("${ghlove.juso.result-type}")
    private String resultType;

    @GetMapping("/admin/juso-popup")
    public String jusoPopup(Model model) {
        model.addAttribute("jusoConfmKey", confmKey);
        model.addAttribute("jusoDomain", domain);
        model.addAttribute("jusoResultType", resultType);
        return "juso/juso-popup";
    }
}
