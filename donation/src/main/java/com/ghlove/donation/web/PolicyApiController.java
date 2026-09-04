package com.ghlove.donation.web;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** storefront(Vue3 SPA)용 개인정보처리방침/저작권정책/이용약관 JSON API -
 * {@link PolicyController}(Thymeleaf)와 완전히 같은 CMS 원문(policy-content/*.html)을
 * 그대로 내려준다. */
@RestController
public class PolicyApiController {

    private static final Map<String, TitledSlug> SLUGS = Map.of(
            "privacy", new TitledSlug("개인정보처리방침", "privacy"),
            "copyright", new TitledSlug("저작권 정책", "copyright"),
            "auth", new TitledSlug("이용약관 동의", "clause"));

    private record TitledSlug(String title, String resourceName) {
    }

    public record PolicyResponse(String title, String content) {
    }

    @GetMapping("/api/policy/{slug}")
    public ResponseEntity<PolicyResponse> policy(@PathVariable String slug) {
        TitledSlug titledSlug = SLUGS.get(slug);
        if (titledSlug == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new PolicyResponse(titledSlug.title(), loadContent(titledSlug.resourceName())));
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
