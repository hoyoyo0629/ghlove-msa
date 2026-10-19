package com.ghlove.donation.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
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

    public MemberClient(@Value("${ghlove.member-service.base-url}") String memberServiceBaseUrl,
                        @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        // member의 /api/users/walk-in, /api/users/{id} 는 내부 전용이라 공유 시크릿을 실어야 통과한다.
        this.restClient = RestClient.builder()
                .baseUrl(memberServiceBaseUrl)
                .defaultHeader("X-Internal-Secret", adminSecret)
                .build();
    }

    /** 회원번호→CI(CI 있는 회원만) - 국세청 전자기부영수증 배치가 AS-IS {@code (select mber_ci from op_user ...)}를 대신해 쓴다. */
    public java.util.Map<Long, String> ciMap(java.util.List<Long> userIds) {
        java.util.Map<Long, String> map = restClient.get()
                .uri(u -> u.path("/api/admin/members/batch/ci-map").queryParam("userIds", userIds).build())
                .retrieve().body(new org.springframework.core.ParameterizedTypeReference<java.util.Map<Long, String>>() {
                });
        return map != null ? map : java.util.Map.of();
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

    /**
     * 같은 사람(CI 동일)이 <b>올해 탈퇴하면서 남긴 기부액</b> - 연간 한도에서 함께 차감한다.
     * AS-IS `DonationVerification.isDonationNormalAmount()`의 `getGMberSecsnSumCntrAmt(mberCi)`
     * 자리다. 탈퇴하면 `USER_ID` 기준 누계가 끊겨 재가입으로 한도를 다시 쓸 수 있게 되는 것을
     * 막는 장치라, <b>조회에 실패하면 0이 아니라 예외를 던져 기부를 막는다</b> - 실패를 0으로
     * 삼키면 그 순간이 곧 한도 우회 창구가 된다.
     */
    public long withdrawnDonationCarryOver(Long userId) {
        try {
            Map<String, Long> body = restClient.get()
                    .uri("/api/users/{id}/withdrawn-donation-carryover", userId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Long>>() {});
            Long amount = body == null ? null : body.get("amount");
            return amount == null ? 0L : amount;
        } catch (RestClientException e) {
            throw new DonationException("기부한도 확인에 필요한 회원 정보를 조회할 수 없습니다. 잠시 후 다시 시도해 주세요.");
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

    public WalkInResult registerWalkIn(String userName, String phoneNumber, String birthday, String address) {
        return registerWalkIn(userName, phoneNumber, birthday, address, null, null);
    }

    /** 오프라인 기부접수(offgive) - 계좌 없는 방문 시민 즉석 가입. member의 UserApiController
     *  가 실제 가입 처리를 하고, 여기선 결과(발급된 아이디/임시비밀번호)를 그대로 전달한다.
     *  mberCi/mberDi(SCI 실명인증 결과)까지 같이 저장해야 다음 접수부터 user-check로
     *  재사용된다(아니면 매번 새 계정이 생긴다). */
    public WalkInResult registerWalkIn(String userName, String phoneNumber, String birthday, String address,
                                        String mberCi, String mberDi) {
        return registerWalkIn(userName, phoneNumber, birthday, address, mberCi, mberDi, null, null, null, null);
    }

    /**
     * 기탁서 등록 - AS-IS insertOffgive가 USER_DETAIL에 우편번호·주소·상세주소·성별까지 저장하고, OP_USER.LOCGOV_CODE에
     * 행정지 지자체(psitnLocgovCode)를 넣는다.
     */
    public WalkInResult registerWalkIn(String userName, String phoneNumber, String birthday, String address,
                                        String mberCi, String mberDi, String post, String addressDetail,
                                        String gender, String locgovCode) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        if (locgovCode != null) {
            body.add("locgovCode", locgovCode);
        }
        if (post != null) {
            body.add("post", post);
        }
        if (addressDetail != null) {
            body.add("addressDetail", addressDetail);
        }
        if (gender != null) {
            body.add("gender", gender);
        }
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
        if (mberCi != null) {
            body.add("mberCi", mberCi);
        }
        if (mberDi != null) {
            body.add("mberDi", mberDi);
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
