package com.ghlove.member.service;

import com.ghlove.member.domain.User;
import com.ghlove.member.domain.UserDetail;
import com.ghlove.member.domain.UserRole;
import com.ghlove.member.domain.UserSns;
import com.ghlove.member.repository.UserDetailRepository;
import com.ghlove.member.repository.UserRepository;
import com.ghlove.member.repository.UserRoleRepository;
import com.ghlove.member.repository.UserSnsRepository;
import com.ghlove.member.service.integration.ExternalIdentity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 디지털원패스/카카오/네이버로 확인된 외부 신원을 기존 회원에 연결하거나, 처음 보는
 * 신원이면 즉시 간편가입 처리한다 (AS-IS는 원패스는 CI, SNS는 OP_USER_SNS로 각각
 * 연결 여부를 판단 - 여기서도 동일한 키로 조회한다).
 */
@Service
@RequiredArgsConstructor
public class ExternalLoginService {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String ROLE_USER = "ROLE_USER";
    private static final String PROVIDER_ONEPASS = "ONEPASS";
    private static final SecureRandom RANDOM = new SecureRandom();
    /** AS-IS 공통코드 LOGIN_PATH 실제값(09.공통코드 목록.xlsx) - provider별 매핑. */
    private static final java.util.Map<String, String> LOGIN_PATH_BY_PROVIDER =
            java.util.Map.of("ONEPASS", "300", "KAKAO", "500", "NAVER", "600",
                    "FINANCE_CERT", "400", "ANYID", "200");
    private static final java.time.format.DateTimeFormatter DATE_FORMAT = java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final UserRepository userRepository;
    private final UserDetailRepository userDetailRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserSnsRepository userSnsRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<User> findLinkedUser(ExternalIdentity identity) {
        if (PROVIDER_ONEPASS.equals(identity.provider())) {
            return userRepository.findByMberCi(identity.externalId());
        }
        return userSnsRepository.findBySnsTypeAndSnsId(identity.provider(), identity.externalId())
                .flatMap(sns -> userRepository.findById(sns.getUserId()));
    }

    @Transactional
    public User linkNewAccount(ExternalIdentity identity) {
        User user = new User();
        user.setLoginId(identity.provider().toLowerCase() + "_" + identity.externalId().replaceAll("[^a-zA-Z0-9]", ""));
        user.setPassword(passwordEncoder.encode(generateRandomPassword()));
        user.setUserName(identity.name() != null ? identity.name() : identity.provider() + " 회원");
        user.setEmail(identity.email());
        user.setStatusCode(STATUS_ACTIVE);
        user.setSbscrbSeCode("GENERAL");
        user.setLoginPathCode(LOGIN_PATH_BY_PROVIDER.getOrDefault(identity.provider(), identity.provider()));
        user.setLoginCount(0);
        user.setLoginFailCount(0);
        user.setCreatedDate(DATE_FORMAT.format(LocalDateTime.now()));
        user.setUpdatedDate(DATE_FORMAT.format(LocalDateTime.now()));
        if (PROVIDER_ONEPASS.equals(identity.provider())) {
            user.setMberCi(identity.externalId());
            user.setMberDn(identity.name());
        }
        User saved = userRepository.save(user);

        UserDetail detail = new UserDetail();
        detail.setUserId(saved.getUserId());
        detail.setLevelId(1);
        detail.setUseFlag("Y");
        if (identity.birthday() != null) {
            detail.setBirthday(identity.birthday());
        }
        userDetailRepository.save(detail);

        userRoleRepository.save(new UserRole(saved.getUserId(), ROLE_USER));

        if (!PROVIDER_ONEPASS.equals(identity.provider())) {
            UserSns sns = new UserSns();
            sns.setSnsType(identity.provider());
            sns.setSnsId(identity.externalId());
            sns.setUserId(saved.getUserId());
            sns.setSnsName(identity.name());
            sns.setEmail(identity.email());
            sns.setCreatedDate(DATE_FORMAT.format(LocalDateTime.now()));
            sns.setCertifiedDate(DATE_FORMAT.format(LocalDateTime.now()));
            userSnsRepository.save(sns);
        }

        return saved;
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$%";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
