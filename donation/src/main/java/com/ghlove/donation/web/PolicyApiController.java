package com.ghlove.donation.web;

import com.ghlove.donation.service.PolicyContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** storefront(Vue3 SPA)용 개인정보처리방침/저작권정책/이용약관 JSON API -
 * {@link PolicyController}(Thymeleaf)와 완전히 같은 본문을 같은 규칙으로 내려준다
 * (admin 약관관리가 원본, 미등록 시 서비스에 들어있는 원문으로 폴백). */
@RestController
@RequiredArgsConstructor
public class PolicyApiController {

    private final PolicyContentService policyContentService;

    public record PolicyResponse(String title, String content) {
    }

    @GetMapping("/api/policy/{slug}")
    public ResponseEntity<PolicyResponse> policy(@PathVariable String slug) {
        return policyContentService.find(slug)
                .map(p -> ResponseEntity.ok(new PolicyResponse(p.title(), p.content())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
