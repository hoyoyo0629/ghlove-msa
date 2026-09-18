package com.ghlove.member.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 회원 가입 정책값.
 *
 * <p>AS-IS는 이 값을 운영관리의 쇼핑몰 설정(`Config.getDeniedId()`)에서 콤마로 구분된 문자열로
 * 읽어 `UserServiceImpl.checkDuplication()`에서 대조한다. MSA는 admin 서비스의 설정 이관이
 * 끝나기 전까지 프로퍼티로 두고, 이관 시점에 이 클래스의 주입원만 교체하면 되도록 사용처를
 * 한 곳으로 모았다.
 */
@Component
public class MemberPolicySettings {

    private final Set<String> deniedLoginIds;

    public MemberPolicySettings(
            @Value("${ghlove.member.denied-login-ids:}") String deniedLoginIds) {
        this.deniedLoginIds = Arrays.stream(deniedLoginIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * 가입 불가 아이디인가. AS-IS는 `userId.trim().equals(user.getLoginId().trim())`으로
     * 대소문자를 구분해 비교하지만, 대문자만 바꿔 우회할 수 있는 검사는 의미가 없으므로
     * 양쪽을 소문자로 맞춰 대조한다.
     */
    public boolean isDenied(String loginId) {
        if (loginId == null) {
            return false;
        }
        return deniedLoginIds.contains(loginId.trim().toLowerCase(Locale.ROOT));
    }
}
