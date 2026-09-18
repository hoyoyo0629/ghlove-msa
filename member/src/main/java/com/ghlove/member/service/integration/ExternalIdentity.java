package com.ghlove.member.service.integration;

/**
 * 외부 연계로 확인된 신원 정보.
 *
 * <p>두 종류가 섞여 들어온다.
 * <ul>
 *   <li><b>본인확인 기반</b>(디지털원패스, 카카오 인증서비스) - {@code ci}가 있다. 실명·생년월일·
 *       휴대폰번호가 확인된 신원이므로 이걸로 기가입자를 찾고(AS-IS도 CI로 찾는다) 회원가입까지
 *       바로 진행할 수 있다.</li>
 *   <li><b>SNS 프로필 기반</b>(네이버 OAuth 로그인, 모의 인증) - {@code ci}가 없다. 공급자별
 *       회원번호({@code externalId})로만 연결한다.</li>
 * </ul>
 */
public record ExternalIdentity(String provider, String externalId, String name, String email,
                               String birthday, String phoneNumber, String gender, String ci) {

    /** CI 없이 공급자 회원번호로만 연결되는 신원(네이버 OAuth 프로필, 모의 인증). */
    public static ExternalIdentity ofProfile(String provider, String externalId, String name,
                                            String email, String birthday) {
        return new ExternalIdentity(provider, externalId, name, email, birthday, null, null, null);
    }

    /** 본인확인으로 CI가 확인된 신원 - 이 값이 있으면 회원 조회/가입이 CI 기준으로 동작한다. */
    public boolean isIdentityVerified() {
        return ci != null && !ci.isBlank();
    }
}
