package com.ghlove.member.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 실제 외부 인증 게이트웨이가 없는 환경에서 테스트를 계속 진행할 수 있게 하는 우회 스위치.
 *
 * <p>회원가입 화면의 "본인인증 없이 진행(테스트용)" 링크와 같은 성격이지만, 이쪽은 훨씬
 * 위험하다. 아이디/비밀번호 찾기의 인증번호 확인을 건너뛰면 <b>이름과 휴대폰번호만 아는
 * 사람이 남의 비밀번호를 바꿀 수 있는 계정 탈취 경로</b>가 된다. 그래서 화면 노출과 서버
 * 처리 양쪽 모두 이 플래그로 막고, 기본값을 false로 두어 설정을 켜지 않은 환경(=운영)에서는
 * 엔드포인트가 존재하더라도 아무 일도 하지 않게 한다 - ExternalLoginController의 모의 인증이
 * 실연계가 켜져 있으면 거부하는 것과 같은 방어 구조다.
 */
@Component
@Slf4j
public class DevBypassSettings {

    private final boolean identityVerificationBypass;

    public DevBypassSettings(
            @Value("${ghlove.dev.identity-verification-bypass:false}") boolean identityVerificationBypass) {
        this.identityVerificationBypass = identityVerificationBypass;
        if (identityVerificationBypass) {
            log.warn("본인인증 우회가 켜져 있습니다(ghlove.dev.identity-verification-bypass=true) - "
                    + "이름+휴대폰번호만으로 아이디 조회/비밀번호 재설정이 가능합니다. 운영에서는 반드시 false여야 합니다.");
        }
    }

    public boolean isIdentityVerificationBypass() {
        return identityVerificationBypass;
    }
}
