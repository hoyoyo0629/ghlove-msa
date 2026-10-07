package com.ghlove.member.web;

import com.ghlove.member.service.PolicyClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 회원가입 약관동의 화면(storefront /signup)의 요약 박스용 API - AS-IS
 * saleson.api.user.JoinController.getPolicyInfo(POST /api/join/getPolicyInfo) 재현.
 * admin 약관관리(OP_POLICY)가 원본이고, 이용약관(타입0)과 개인정보 수집·이용 동의(타입6)만
 * 돌려준다 - AS-IS agree.vue가 실제로 쓰는 두 항목만이다(같이 조회되는 "개인정보제3자동의"
 * 타입4는 AS-IS 화면에도 안 쓰여 여기서도 뺐다).
 */
@RestController
@RequiredArgsConstructor
public class SignupPolicyApiController {

    private final PolicyClient policyClient;

    public record SignupPolicyResponse(PolicyClient.PolicyContent terms, PolicyClient.PolicyContent collectionAgree) {
    }

    @GetMapping("/api/signup-policies")
    public SignupPolicyResponse signupPolicies() {
        return new SignupPolicyResponse(
                policyClient.currentPolicy("clause").orElse(null),
                policyClient.currentPolicy("collection").orElse(null));
    }
}
