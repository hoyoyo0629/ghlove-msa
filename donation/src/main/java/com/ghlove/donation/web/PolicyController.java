package com.ghlove.donation.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * footer 5개 메뉴 중 개인정보처리방침/저작권정책/이용약관 (AS-IS policy/privacy.html,
 * copyright.html, auth.html). AS-IS는 이 셋 다 서버 CMS(OP_POLICY류)에 저장된 위지윅
 * 본문을 API(/api/policy/protect, /copyright, /clause)로 받아와 v-html로 그대로 뿌리는
 * 얇은 셸이다 - 로그인이나 다른 모델 데이터가 필요 없는 순수 정적 콘텐츠라는 점에서
 * GuideController와 동일한 패턴을 따른다. 다만 본문 자체가 매우 커서(특히 개인정보처리방침은
 * 라벨링 아이콘 6개 + 조항 14개 포함 실제 운영 문서 원문 그대로라 130만자) Java 소스에
 * 박아넣지 않고 리소스 파일로 분리해 읽어온다.
 */
@Controller
@Slf4j
public class PolicyController {

    @GetMapping("/policy/privacy")
    public String privacy(Model model) {
        model.addAttribute("title", "개인정보처리방침");
        model.addAttribute("content", loadContent("privacy"));
        return "policy/privacy";
    }

    @GetMapping("/policy/copyright")
    public String copyright(Model model) {
        model.addAttribute("title", "저작권 정책");
        model.addAttribute("content", loadContent("copyright"));
        return "policy/copyright";
    }

    @GetMapping("/policy/auth")
    public String terms(Model model) {
        model.addAttribute("title", "이용약관 동의");
        model.addAttribute("content", loadContent("clause"));
        return "policy/auth";
    }

    private String loadContent(String name) {
        try {
            byte[] bytes = new ClassPathResource("policy-content/" + name + ".html").getContentAsByteArray();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
