package com.ghlove.member.service;

import com.ghlove.member.domain.User;
import com.ghlove.member.domain.UserDetail;
import com.ghlove.member.domain.UserRole;
import com.ghlove.member.domain.UserSns;
import com.ghlove.member.repository.UserDetailRepository;
import com.ghlove.member.repository.UserRepository;
import com.ghlove.member.repository.UserRoleRepository;
import com.ghlove.member.repository.UserSnsRepository;
import com.ghlove.member.service.integration.ExternalAuthException;
import com.ghlove.member.service.integration.ExternalIdentity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * 디지털원패스/카카오 인증서비스/네이버로 확인된 외부 신원을 기존 회원에 연결하거나, 처음 보는
 * 신원이면 즉시 간편가입 처리한다 (AS-IS는 원패스·카카오 인증서비스는 CI, SNS 프로필 로그인은
 * OP_USER_SNS로 각각 연결 여부를 판단 - 여기서도 동일한 키로 조회한다).
 */
@Service
@RequiredArgsConstructor
public class ExternalLoginService {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String ROLE_USER = "ROLE_USER";
    private static final String PROVIDER_ONEPASS = "ONEPASS";
    /** AS-IS KakaoLinkServiceImpl:702가 `setPasswordType("P")`를 거는 대상 - loginAuthType이
     *  KAKAO/NAVER인 경우다. 원패스·금융인증서는 각자 경로라 P를 받지 않는다. */
    private static final java.util.Set<String> SNS_PASSWORD_TYPE_PROVIDERS =
            java.util.Set.of("KAKAO", "NAVER");
    /** AS-IS passwordType: N=일반, T=임시, P=SNS(카카오/네이버). P는 비밀번호 만료·실패잠금을
     *  타지 않는다 - SNS 회원은 애초에 비밀번호로 로그인하지 않기 때문이다. */
    private static final String PASSWORD_TYPE_SNS = "P";
    private static final SecureRandom RANDOM = new SecureRandom();
    /** AS-IS 공통코드 LOGIN_PATH 실제값(09.공통코드 목록.xlsx) - provider별 매핑. */
    private static final java.util.Map<String, String> LOGIN_PATH_BY_PROVIDER =
            java.util.Map.of("ONEPASS", "300", "KAKAO", "500", "NAVER", "600",
                    "FINANCE_CERT", "400", "ANYID", "200");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter BIRTHDAY_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    /** AS-IS와 동일 - 만 14세 미만은 본인인증 기반 간편가입을 막는다. */
    private static final int MIN_JOIN_AGE = 14;

    private final UserRepository userRepository;
    private final UserDetailRepository userDetailRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserSnsRepository userSnsRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.ghlove.member.event.MemberEventPublisher eventPublisher;

    /** 외부 신원 확인 결과 - {@code newlyJoined}는 AS-IS KakaoLinkServiceImpl이 돌려주던
     *  code(JOIN_MEMBER/LOGIN)와 같은 구분이다. 회원가입 화면에서 들어온 인증은 이 값이
     *  true일 때만 "인증 로그인 통한 회원가입 완료" 안내를 띄운다. */
    public record LinkResult(User user, boolean newlyJoined) {}

    /**
     * 이미 연결된 신원이면 그 회원으로 로그인, 처음 보는 신원이면 즉시 간편가입한다
     * (AS-IS kakaoLinkJoinAndLoginProcess의 분기와 동일). 호출측이 "방금 가입한 것인지"를
     * 알아야 해서 findLinkedUser/linkNewAccount를 각자 조합하지 않고 여기로 모았다.
     *
     * @param authLabel 실패 안내문에 쓰는 인증수단 이름("카카오톡"/"네이버"/...)
     */
    @Transactional
    public LinkResult linkOrJoin(ExternalIdentity identity, String authLabel) {
        Optional<User> linked = findLinkedUser(identity);
        if (linked.isPresent()) {
            // AS-IS는 신규가입뿐 아니라 SNS로 로그인할 때마다 passwordType을 P로 다시 찍는다
            // (KakaoLinkServiceImpl:702). 일반 로그인으로 가입한 회원이 나중에 SNS를 연결한
            // 경우까지 만료 정책에서 빠지도록 하는 처리라 재로그인 시에도 그대로 따른다.
            return new LinkResult(stampSnsPasswordType(linked.get(), identity.provider()), false);
        }
        if (identity.isIdentityVerified()) {
            // 본인확인 기반(카카오 인증서비스/디지털원패스) 신규가입만 해당 - AS-IS는 SNS 프로필
            // 로그인에는 이 검사를 걸지 않았다(받아올 정보 자체가 없다).
            requireJoinableIdentity(identity, authLabel);
        }
        return new LinkResult(linkNewAccount(identity), true);
    }

    /**
     * AS-IS kakaoLinkJoinAndLoginProcess의 신규가입 분기 검사 - 필수정보 누락과 14세 미만.
     * 안내문은 AS-IS KakaoLinkController가 ApiError별로 내려준 문구를 그대로 쓴다.
     */
    private void requireJoinableIdentity(ExternalIdentity identity, String authLabel) {
        // AS-IS 문구는 "카카오 계정 정보에 ..." 였다(그 경로가 카카오 전용이었다) - 다른 수단도
        // 같은 검사를 타므로 앞머리만 수단에 맞게 바꾼다.
        String source = "KAKAO".equals(identity.provider()) ? "카카오 계정 정보에" : authLabel + " 인증 정보에";
        if (isBlank(identity.name())) {
            throw new ExternalAuthException(source + " 이름 정보가 없어 회원가입이 불가능합니다.");
        }
        if (isBlank(identity.email())) {
            throw new ExternalAuthException(source + " 이메일 정보가 없어 회원가입이 불가능합니다.");
        }
        if (isBlank(identity.phoneNumber())) {
            throw new ExternalAuthException(source + " 전화번호 정보가 없어 회원가입이 불가능합니다.");
        }
        LocalDate birthDate = parseBirthday(identity.birthday());
        if (birthDate == null) {
            throw new ExternalAuthException(source + " 생년월일 정보가 없어 회원가입이 불가능합니다.");
        }
        if (ChronoUnit.YEARS.between(birthDate, LocalDate.now()) < MIN_JOIN_AGE) {
            throw new ExternalAuthException(
                    MIN_JOIN_AGE + "세 미만은 " + authLabel + " 인증 로그인으로 회원가입이 불가능합니다.");
        }
    }

    private LocalDate parseBirthday(String birthday) {
        if (isBlank(birthday) || birthday.length() != 8) {
            return null;
        }
        try {
            return LocalDate.parse(birthday, BIRTHDAY_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * CI가 확인된 신원은 CI로 찾는다 - 카카오 인증서비스/디지털원패스/휴대폰인증이 모두 같은
     * 사람에게 같은 CI를 주므로, 다른 수단으로 이미 가입한 회원도 여기서 걸러진다(AS-IS도
     * userRepository.findByMberCi 하나로 판단했다). CI가 없는 SNS 프로필 로그인만 공급자
     * 회원번호(OP_USER_SNS)로 찾는다.
     */
    public Optional<User> findLinkedUser(ExternalIdentity identity) {
        if (identity.isIdentityVerified()) {
            // 탈퇴 후 같은 CI로 재가입이 가능해 한 CI에 여러 계정이 남을 수 있다. 로그인시켜야
            // 하는 것은 <b>현재 정상인 계정</b>이므로 상태로 좁혀 찾는다(단건 조회로 두면
            // 재가입 회원에게서 NonUniqueResultException이 난다).
            Optional<User> byCi = userRepository
                    .findFirstByMberCiAndStatusCodeOrderByUserIdDesc(identity.ci(), STATUS_ACTIVE);
            if (byCi.isPresent()) {
                return byCi;
            }
        }
        if (PROVIDER_ONEPASS.equals(identity.provider())) {
            return Optional.empty();
        }
        return userSnsRepository.findBySnsTypeAndSnsId(identity.provider(), identity.externalId())
                .flatMap(sns -> userRepository.findById(sns.getUserId()));
    }

    /** SNS(카카오/네이버) 계정에 passwordType='P'를 찍는다 - 이미 P면 저장하지 않는다. */
    private User stampSnsPasswordType(User user, String provider) {
        if (!SNS_PASSWORD_TYPE_PROVIDERS.contains(provider)
                || PASSWORD_TYPE_SNS.equals(user.getPasswordType())) {
            return user;
        }
        user.setPasswordType(PASSWORD_TYPE_SNS);
        return userRepository.save(user);
    }

    @Transactional
    public User linkNewAccount(ExternalIdentity identity) {
        User user = new User();
        user.setLoginId(generateLoginId());
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
        if (identity.isIdentityVerified()) {
            user.setMberCi(identity.ci());
            user.setMberDn(identity.name());
            if ("KAKAO".equals(identity.provider())) {
                user.setKakaoUserKey(identity.externalId());
            }
        }
        // OP_USER.PASSWORD_TYPE은 NOT NULL이다 - SNS(카카오/네이버)는 'P'(만료·실패잠금 제외),
        // 그 밖의 외부 인증수단(원패스/금융인증서/간편인증)은 AS-IS 기본값 'N'을 넣는다
        // (AS-IS UserServiceImpl의 일반 계정 생성 경로도 setPasswordType("N")). 이 값을 비우면
        // 원패스 등 신규 간편가입이 INSERT 단계에서 NOT NULL 위반으로 실패한다.
        user.setPasswordType(SNS_PASSWORD_TYPE_PROVIDERS.contains(identity.provider())
                ? PASSWORD_TYPE_SNS : "N");
        User saved = userRepository.save(user);

        UserDetail detail = new UserDetail();
        detail.setUserId(saved.getUserId());
        detail.setLevelId(1);
        detail.setUseFlag("Y");
        if (identity.birthday() != null) {
            detail.setBirthday(identity.birthday());
        }
        // AS-IS userModifyDataSet() - 본인확인으로 받은 휴대폰/성별을 상세에 남긴다. CI와 카카오
        // 회원번호는 OP_USER 컬럼이라 위에서 User에 넣었다. 수신동의를 전부 동의("0")로 시작하는
        // 것도 AS-IS와 동일하다.
        if (identity.phoneNumber() != null) {
            detail.setPhoneNumber(identity.phoneNumber());
        }
        if (identity.gender() != null) {
            detail.setGender(identity.gender());
        }
        if (identity.isIdentityVerified()) {
            detail.setReceiveEmail("0");
            detail.setReceiveSms("0");
            detail.setReceiveKakao("0");
        }
        userDetailRepository.save(detail);

        userRoleRepository.save(new UserRole(saved.getUserId(), ROLE_USER));

        // CI로 연결되는 수단(원패스/카카오 인증서비스)은 OP_USER.MBER_CI가 연결키라 SNS 행을
        // 만들지 않는다 - SNS 프로필 로그인(네이버 OAuth)만 여기로 들어온다.
        if (!identity.isIdentityVerified() && !PROVIDER_ONEPASS.equals(identity.provider())) {
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

        // 외부 인증(원패스/카카오/네이버) 경유 신규 간편가입도 회원가입 완료 이벤트를 발행한다
        // (가입경로 loginPathCode만 다름) - MemberRegistered.
        eventPublisher.publishMemberJoined(saved.getUserId(), saved.getLoginId(),
                saved.getLoginPathCode(), saved.getSbscrbSeCode());

        return saved;
    }

    /**
     * AS-IS randomUserId() - 첫 글자는 영문 소문자, 이어서 무작위 4~10자, 중복이면 다시 뽑는다.
     * 외부 인증 가입자는 아이디를 직접 정하지 않으므로 서버가 발급한다. CI/공급자 회원번호를
     * 아이디에 그대로 박아넣지 않는 것도 AS-IS와 같다(식별정보가 아이디로 노출되면 안 된다).
     */
    private String generateLoginId() {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        for (int attempt = 0; attempt < 100; attempt++) {
            StringBuilder sb = new StringBuilder();
            sb.append((char) ('a' + RANDOM.nextInt(26)));
            int length = 4 + RANDOM.nextInt(7); // 뒤에 4~10자
            for (int i = 0; i < length; i++) {
                sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
            }
            String candidate = sb.toString();
            if (userRepository.findByLoginId(candidate).isEmpty()) {
                return candidate;
            }
        }
        throw new IllegalStateException("사용 가능한 아이디를 발급하지 못했습니다.");
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
