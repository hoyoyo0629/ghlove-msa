package com.ghlove.donation.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

/**
 * 기부확인증(성명/생년월일 표기) 발급 시에만 쓰이는 읽기 전용 조회 - AS-IS에서는
 * 로그인 세션(UserUtils.getUser())으로 즉시 얻던 값을, 게이트웨이/세션 연동이 없는
 * 이 MSA에서는 order의 GiftClient와 동일한 방식으로 동기 REST 조회한다.
 */
@Component
public class MemberClient {

    private final RestClient restClient;

    public MemberClient(@Value("${ghlove.member-service.base-url}") String memberServiceBaseUrl) {
        this.restClient = RestClient.create(memberServiceBaseUrl);
    }

    public MemberInfo fetch(Long userId) {
        try {
            return restClient.get()
                    .uri("/api/users/{id}", userId)
                    .retrieve()
                    .body(MemberInfo.class);
        } catch (RestClientException e) {
            throw new DonationException("회원 정보를 조회할 수 없습니다. (userId=" + userId + ")");
        }
    }

    /** 목록 화면의 "OOO 회원님" 배지 등 조회 실패해도 화면 전체가 깨지면 안 되는 표시용 조회. */
    public MemberInfo fetchOrNull(Long userId) {
        try {
            return fetch(userId);
        } catch (DonationException e) {
            return null;
        }
    }

    /** 오프라인 기부접수(offgive) - 계좌 없는 방문 시민 즉석 가입. member의 UserApiController
     *  가 실제 가입 처리를 하고, 여기선 결과(발급된 아이디/임시비밀번호)를 그대로 전달한다. */
    public WalkInResult registerWalkIn(String userName, String phoneNumber, String birthday, String address) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("userName", userName);
        if (phoneNumber != null) {
            body.add("phoneNumber", phoneNumber);
        }
        if (birthday != null) {
            body.add("birthday", birthday);
        }
        if (address != null) {
            body.add("address", address);
        }
        try {
            return restClient.post().uri("/api/users/walk-in")
                    .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve().body(WalkInResult.class);
        } catch (RestClientResponseException e) {
            try {
                Map<?, ?> errorBody = e.getResponseBodyAs(Map.class);
                Object msg = errorBody != null ? errorBody.get("message") : null;
                throw new DonationException(msg != null ? msg.toString() : "회원가입에 실패했습니다.");
            } catch (RuntimeException parseError) {
                throw new DonationException("회원가입에 실패했습니다.");
            }
        }
    }

    public record WalkInResult(Long userId, String loginId, String tempPassword) {
    }
}
