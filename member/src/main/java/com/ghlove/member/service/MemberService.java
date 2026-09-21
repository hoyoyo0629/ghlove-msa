package com.ghlove.member.service;

import com.ghlove.member.domain.LoginLog;
import com.ghlove.member.domain.User;
import com.ghlove.member.domain.UserChangeLog;
import com.ghlove.member.domain.UserDataDestructionLog;
import com.ghlove.member.domain.UserDetail;
import com.ghlove.member.domain.UserRole;
import com.ghlove.member.repository.CommonCodeRepository;
import com.ghlove.member.repository.LoginLogRepository;
import com.ghlove.member.repository.UserChangeLogRepository;
import com.ghlove.member.repository.UserDetailRepository;
import com.ghlove.member.repository.UserRepository;
import com.ghlove.member.repository.UserRoleRepository;
import com.ghlove.member.service.integration.NotificationClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_LOCKED = "LOCKED";
    /** AS-IS PASSWORD_TYPE - 'N' 정상, 'T' 발급된 임시비밀번호(반드시 변경). */
    private static final String PASSWORD_TYPE_NORMAL = "N";
    private static final String PASSWORD_TYPE_TEMPORARY = "T";
    /** AS-IS passwordType P = SNS(카카오/네이버) 회원. 비밀번호 만료(AuthController:1232,
     *  JwtTokenAuthenticationFilter:542)와 실패 5회 잠금(AuthController:1200)을 타지 않는다. */
    private static final String PASSWORD_TYPE_SNS = "P";
    /** SYSTEM_CONFIG/LIFE_TIME_PASSWORD 미설정 시 AS-IS와 같은 폴백(일). */
    private static final long DEFAULT_PASSWORD_LIFE_TIME_DAYS = 180L;
    private static final String STATUS_WITHDRAWN = "WITHDRAWN";
    private static final String STATUS_DORMANT = "DORMANT";
    private static final String USER_TYPE_GENERAL = "GENERAL";
    /** AS-IS 공통코드 LOGIN_PATH 실제값(09.공통코드 목록.xlsx) - 100:ID/PWD. */
    private static final String LOGIN_PATH_IDPW = "100";
    private static final String ROLE_USER = "ROLE_USER";
    private static final int MAX_LOGIN_FAIL_COUNT = 5;
    private static final int DEFAULT_DORMANT_INACTIVE_DAYS = 365;
    /** AS-IS 레거시 컬럼 컨벤션(VARCHAR(14)) - User/LoginLog/UserChangeLog의 날짜 필드가 전부 이 형식이다. */
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final UserRepository userRepository;
    private final UserDetailRepository userDetailRepository;
    private final CommonCodeRepository commonCodeRepository;
    private final LoginLogRepository loginLogRepository;
    private final UserChangeLogRepository userChangeLogRepository;
    private final UserRoleRepository userRoleRepository;
    private final com.ghlove.member.repository.UserDataDestructionLogRepository userDataDestructionLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationClient notificationClient;
    private final MemberPolicySettings memberPolicySettings;
    private final DevBypassSettings devBypassSettings;
    private final DonationClient donationClient;
    private final PointClient pointClient;
    private final GiftClient giftClient;
    private final com.ghlove.member.repository.MberSecsnRepository mberSecsnRepository;
    private final com.ghlove.member.repository.UserCiRepository userCiRepository;

    /**
     * 아이디를 쓸 수 있는가 - AS-IS `UserServiceImpl.checkDuplication()` 재현.
     * ①운영설정의 가입 불가 아이디 목록 대조 ②회원 테이블 조회. AS-IS는 둘 중 하나라도
     * 걸리면 같은 `isOccupiedId`를 돌려주므로, 금지 아이디도 화면에는 "이미 사용중"으로
     * 보인다(`JoinController.getUserInfoByUserId`가 그 결과를 idCnt로 그대로 내려준다).
     */
    public boolean loginIdAvailable(String loginId) {
        if (loginId == null || loginId.isBlank()) {
            return false;
        }
        if (memberPolicySettings.isDenied(loginId)) {
            return false;
        }
        return userRepository.findByLoginId(loginId).isEmpty();
    }

    /** No-hardcoding principle: code labels always come from OP_COMMON_CODE, never a Java enum/switch. */
    public Map<String, String> codesOf(String codeType) {
        List<com.ghlove.member.domain.CommonCode> codes =
                commonCodeRepository.findByCodeTypeAndLanguageAndUseYnOrderByOrdering(codeType, "ko", "Y");
        return codes.stream().collect(Collectors.toMap(
                com.ghlove.member.domain.CommonCode::getId,
                com.ghlove.member.domain.CommonCode::getLabel,
                (a, b) -> a, java.util.LinkedHashMap::new));
    }

    public List<String> rolesOf(Long userId) {
        return userRoleRepository.findByUserId(userId).stream()
                .map(UserRole::getAuthority)
                .toList();
    }

    @Transactional
    /**
     * 본인인증 결과 검사 - AS-IS `JoinController.join()`의 가입 전 관문 두 가지를 옮긴 것.
     *
     * <p>①`mberCi`/`mberDi`/생년월일이 하나라도 비면 `BAD_REQUEST`(`JoinController:165~170`).
     * 즉 AS-IS는 <b>본인인증 없이는 가입이 성립하지 않는다</b>.
     * ②이미 같은 CI로 가입한 회원이 있으면 `DUPLICATION_CI_JOIN_USER`(`:186~193`) —
     * 실명 1인당 계정 하나만 허용하는 정책이다.
     *
     * <p>이 환경은 본인인증 게이트웨이가 열려 있지 않아 일반 가입 경로로는 CI가 들어오지
     * 않는다. 그래서 ①은 {@link DevBypassSettings} 우회가 켜진 동안만 건너뛰고(운영 기본값
     * false에서는 AS-IS와 똑같이 막힌다), ②는 <b>CI가 있을 때 항상</b> 검사한다 - 카카오
     * 인증서비스처럼 실제로 CI가 들어오는 경로에서는 우회 여부와 무관하게 막아야 한다.
     */
    private void requireVerifiedIdentity(SignupForm form) {
        String ci = trimToNull(form.getMberCi());
        if (ci != null) {
            // AS-IS `getUserInfoByCi`는 `status_code = '9'`(정상)인 계정만 조회한다
            // (`user-mapper.xml`). 상태코드는 1:가입대기 2:차단 3:탈퇴 4:휴면 9:정상
            // (`GeneralCustomer.java:30`). 즉 <b>탈퇴한 회원의 CI로는 재가입이 가능</b>하며,
            // 그래야 탈퇴 시 남긴 `G_MBER_SECSN` 기부액을 재가입 계정의 연간 한도에서 빼는
            // 장치가 의미를 갖는다(그 표의 존재 자체가 재가입을 전제한다).
            userRepository.findFirstByMberCiAndStatusCodeOrderByUserIdDesc(ci, STATUS_ACTIVE)
                    .ifPresent(u -> {
                        throw new MemberException("이미 가입된 본인인증 정보입니다. 아이디 찾기를 이용해 주세요.");
                    });
        }
        if (devBypassSettings.isIdentityVerificationBypass()) {
            return;
        }
        if (ci == null || trimToNull(form.getMberDi()) == null || trimToNull(form.getBirthday()) == null) {
            throw new MemberException("본인인증을 완료해야 회원가입을 할 수 있습니다.");
        }
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    public User signup(SignupForm form) {
        if (!form.getPassword().equals(form.getPasswordConfirm())) {
            throw new MemberException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }
        requireVerifiedIdentity(form);
        if (!loginIdAvailable(form.getLoginId())) {
            throw new MemberException("이미 사용 중인 아이디입니다.");
        }

        User user = new User();
        user.setLoginId(form.getLoginId());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        stampPasswordChanged(user);
        user.setUserName(form.getUserName());
        user.setEmail(form.getEmail());
        user.setStatusCode(STATUS_ACTIVE);
        user.setSbscrbSeCode(USER_TYPE_GENERAL);
        user.setLoginPathCode(LOGIN_PATH_IDPW);
        user.setLoginCount(0);
        user.setLoginFailCount(0);
        // 본인인증을 거쳐 온 가입이면 CI/DI를 남긴다 - 이후 가입의 1인 1계정 검사와
        // SNS 연동 조회가 이 값을 기준으로 돈다(AS-IS userModifyDataSet:441~447).
        user.setMberCi(trimToNull(form.getMberCi()));
        user.setMberDi(trimToNull(form.getMberDi()));
        user.setCreatedDate(now());
        user.setUpdatedDate(now());
        User saved = userRepository.save(user);

        UserDetail detail = new UserDetail();
        detail.setUserId(saved.getUserId());
        detail.setLevelId(1);
        detail.setPhoneNumber(form.getPhoneNumber());
        // <input type=date>는 yyyy-MM-dd로 넘어오는데 BIRTHDAY 컬럼은 다른 서비스(예: donation의
        // 기부확인증)에서 yyyyMMdd로 파싱하므로 하이픈을 제거해서 저장한다.
        detail.setBirthday(form.getBirthday() != null ? form.getBirthday().replace("-", "") : null);
        detail.setPost(form.getPost());
        detail.setAddress(form.getAddress());
        detail.setAddressDetail(form.getAddressDetail());
        detail.setUseFlag("Y");
        userDetailRepository.save(detail);

        userRoleRepository.save(new UserRole(saved.getUserId(), ROLE_USER));

        // 가입 환영 알림톡 - best-effort, 실패해도 가입 자체는 이미 커밋된 뒤라 막지 않는다.
        notificationClient.sendAlimtalk(form.getPhoneNumber(), "WELCOME_SIGNUP",
                Map.of("userName", form.getUserName()));

        return saved;
    }

    /**
     * 오프라인 기부 접수 시 계좌가 없는 방문 시민을 위한 즉석 가입 (AS-IS
     * OffgiveServiceImpl.insertOffgive()의 "계좌 없으면 즉석 생성" 로직 재현). 아이디/
     * 비밀번호를 본인이 정하는 signup()과 달리, 여기서는 admin 콘솔의 오프라인 접수
     * 담당자가 대신 등록하므로 둘 다 랜덤 생성해 반환한다(ManagerRequestService의
     * 임시비밀번호 발급과 동일한 패턴) - 담당자가 그 자리에서 시민에게 안내한다.
     */
    @Transactional
    public WalkInResult registerWalkIn(String userName, String phoneNumber, String birthday, String address) {
        if (userName == null || userName.isBlank()) {
            throw new MemberException("이름을 입력해 주세요.");
        }
        String loginId = generateLoginId();
        String tempPassword = generateTempPassword();

        User user = new User();
        user.setLoginId(loginId);
        user.setPassword(passwordEncoder.encode(tempPassword));
        // 발급된 임시비밀번호 - 다음 로그인에서 변경을 요구한다(AS-IS PASSWORD_TYPE='T').
        user.setPasswordExpiredDate(newPasswordExpiredDate());
        user.setPasswordType(PASSWORD_TYPE_TEMPORARY);
        user.setUserName(userName);
        user.setStatusCode(STATUS_ACTIVE);
        user.setSbscrbSeCode(USER_TYPE_GENERAL);
        user.setLoginPathCode(LOGIN_PATH_IDPW);
        user.setLoginCount(0);
        user.setLoginFailCount(0);
        user.setCreatedDate(now());
        user.setUpdatedDate(now());
        User saved = userRepository.save(user);

        UserDetail detail = new UserDetail();
        detail.setUserId(saved.getUserId());
        detail.setLevelId(1);
        detail.setPhoneNumber(phoneNumber);
        detail.setBirthday(birthday != null ? birthday.replace("-", "") : null);
        detail.setAddress(address);
        detail.setUseFlag("Y");
        userDetailRepository.save(detail);

        userRoleRepository.save(new UserRole(saved.getUserId(), ROLE_USER));

        return new WalkInResult(saved.getUserId(), loginId, tempPassword);
    }

    public record WalkInResult(Long userId, String loginId, String tempPassword) {
    }

    private static String generateLoginId() {
        String digits = "23456789";
        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder("walkin");
        for (int i = 0; i < 6; i++) {
            sb.append(digits.charAt(random.nextInt(digits.length())));
        }
        return sb.toString();
    }

    /**
     * noRollbackFor: a failed login attempt still needs to persist the fail-count
     * increment / lock transition and the audit log row even though a
     * MemberException is thrown to report the failure to the caller - Spring's
     * default rollback-on-unchecked-exception would otherwise undo both.
     */
    @Transactional(noRollbackFor = MemberException.class)
    public User login(String loginId, String rawPassword, String remoteAddr) {
        return completeLogin(checkCredentials(loginId, rawPassword, remoteAddr), remoteAddr);
    }

    /**
     * SFR-002 "다중 인증체계(MFA) 선택 적용" 로그인 진입점 - 아이디/비번까지만 확인하고,
     * MFA를 켜둔 회원(User.mfaEnabled='Y')이면 로그인을 아직 완료하지 않은 채 인증번호
     * 발송 결과만 돌려준다(완료는 {@link #verifyMfaAndCompleteLogin}에서). MFA를 안 켠
     * 회원은 기존과 동일하게 이 한 번의 호출로 바로 로그인이 끝난다.
     */
    public record LoginOutcome(User user, boolean mfaRequired, String maskedPhone, String devCode, boolean dormant,
                                String passwordChangeCode) {
    }

    public LoginOutcome loginWithMfaCheck(String loginId, String rawPassword, String remoteAddr) {
        User user = checkCredentials(loginId, rawPassword, remoteAddr);
        // AS-IS(op.saleson.js:1169): 본인확인은 끝났으나 휴면회원이면 로그인/MFA를 진행하지 않고
        // SLEEP_USER로 신호해 로그인 화면에서 휴면해제를 물어본다. completeLogin(최종로그인일/횟수
        // 갱신)도 아직 하지 않는다 - 해제 후 재로그인 시점에 기록된다.
        if (STATUS_DORMANT.equals(user.getStatusCode())) {
            return new LoginOutcome(user, false, null, null, true, null);
        }
        // AS-IS(op.saleson.js:1182/1201): 본인확인 후 비밀번호 만료/임시비번이면 로그인/ MFA를 진행하지
        // 않고 PASSWORD_EXPIRED/PASSWORD_TEMP로 신호해 변경을 요구한다. completeLogin도 아직 하지 않는다.
        String pwCode = passwordChangeCode(user);
        if (pwCode != null) {
            return new LoginOutcome(user, false, null, null, false, pwCode);
        }
        if (!"Y".equals(user.getMfaEnabled())) {
            return new LoginOutcome(completeLogin(user, remoteAddr), false, null, null, false, null);
        }
        UserDetail detail = userDetailRepository.findById(user.getUserId()).orElse(null);
        String phoneNumber = detail != null ? detail.getPhoneNumber() : null;
        if (phoneNumber == null || phoneNumber.isBlank()) {
            // 휴대폰번호가 없으면 MFA를 켜둔 의미가 없다 - 인증수단이 없으니 그냥 통과시킨다
            // (설정 화면에서 휴대폰번호 없이는 MFA를 켤 수 없게 막는 게 근본 해결이지만,
            // 이미 켜둔 상태에서 번호를 지운 경우까지 로그인 자체를 막아버리면 계정이 잠긴다).
            return new LoginOutcome(completeLogin(user, remoteAddr), false, null, null, false, null);
        }
        String code = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        notificationClient.sendAlimtalk(phoneNumber, "LOGIN_MFA", Map.of("code", code));
        pendingMfaCodes.put(user.getUserId(), new PendingMfaCode(code, LocalDateTime.now(), remoteAddr));
        return new LoginOutcome(user, true, maskPhone(phoneNumber), notificationClient.enabled ? null : code, false, null);
    }

    /** MFA 2단계 - 인증번호가 맞으면 그제서야 로그인을 완료(로그인횟수/최종로그인일시/성공로그)한다. */
    public User verifyMfaAndCompleteLogin(Long userId, String inputCode) {
        PendingMfaCode pending = pendingMfaCodes.get(userId);
        if (pending == null || pending.issuedAt.isBefore(LocalDateTime.now().minusMinutes(5))) {
            pendingMfaCodes.remove(userId);
            throw new MemberException("인증 시간이 초과되었습니다. 다시 로그인해 주세요.");
        }
        if (inputCode == null || !inputCode.trim().equals(pending.code)) {
            throw new MemberException("인증번호가 일치하지 않습니다.");
        }
        pendingMfaCodes.remove(userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MemberException("회원 정보를 찾을 수 없습니다."));
        return completeLogin(user, pending.remoteAddr);
    }

    private record PendingMfaCode(String code, LocalDateTime issuedAt, String remoteAddr) {
    }

    /** MFA 대기중인 코드 저장소 - 관리자 콘솔의 ManagerLoginEmail(DB 테이블)과 달리 일반회원
     *  로그인은 트래픽이 훨씬 많아 매 로그인 시도마다 행을 쌓는 대신 인메모리로 둔다(5분
     *  TTL이라 오래 쌓이지 않는다 - 서버 재시작 시 대기중이던 로그인만 다시 하면 된다). */
    private final java.util.Map<Long, PendingMfaCode> pendingMfaCodes = new java.util.concurrent.ConcurrentHashMap<>();

    /** 1단계: 아이디/비번 확인(실패 횟수 누적/계정 잠금 포함) - MFA 여부와 무관하게 공통. */
    private User checkCredentials(String loginId, String rawPassword, String remoteAddr) {
        User user = userRepository.findByLoginId(loginId).orElse(null);
        if (user == null) {
            recordLoginLog(loginId, "N", remoteAddr, "존재하지 않는 아이디");
            throw new MemberException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        // AS-IS(op.saleson.js:1169)는 휴면회원의 로그인을 에러로 막지 않는다 - 아이디/비번으로
        // 본인확인까지 통과시킨 뒤 로그인 응답에 SLEEP_USER 코드를 실어보내 그 자리에서 휴면해제
        // 여부를 묻는다. 그래서 휴면(4)만 여기서 막지 않고 비밀번호 검증까지 내려보낸다.
        // 나머지 차단상태(가입대기/차단/탈퇴/잠금)는 AS-IS와 동일하게 즉시 에러로 막는다.
        boolean dormant = STATUS_DORMANT.equals(user.getStatusCode());
        if (!STATUS_ACTIVE.equals(user.getStatusCode()) && !dormant) {
            recordLoginLog(loginId, "N", remoteAddr, "차단된 계정 상태: " + user.getStatusCode());
            throw new MemberException(statusBlockedMessage(user.getStatusCode()));
        }

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            int failCount = (user.getLoginFailCount() == null ? 0 : user.getLoginFailCount()) + 1;
            user.setLoginFailCount(failCount);
            user.setLoginTryDate(now());

            String memo = "비밀번호 불일치";
            // AS-IS AuthController:1200 `!"P".equals(passwordType) && failCount >= 5` - SNS 회원은
            // 잠금 예외를 던지지 않는다. 실패 횟수는 그대로 쌓되 계정을 잠그지는 않는다.
            boolean justLocked = failCount >= MAX_LOGIN_FAIL_COUNT
                    && !PASSWORD_TYPE_SNS.equals(user.getPasswordType());
            if (justLocked) {
                user.setStatusCode(STATUS_LOCKED);
                memo = "로그인 실패 횟수 초과로 계정 잠금";
            }
            userRepository.save(user);
            recordLoginLog(loginId, "N", remoteAddr, memo);

            if (justLocked) {
                throw new MemberException("로그인 실패 횟수를 초과하여 계정이 잠겼습니다. 비밀번호 찾기를 통해 잠금을 해제할 수 있습니다.");
            }
            throw new MemberException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        // 휴면회원은 본인확인까지만 하고 로그인/오프라인담당자 검사는 진행하지 않는다 -
        // loginWithMfaCheck가 이 상태를 보고 SLEEP_USER(휴면해제 확인)로 분기한다.
        if (dormant) {
            return user;
        }
        rejectOfflineManager(user, remoteAddr);
        return user;
    }

    /** AS-IS 오프라인 담당자 권한 - 이 계정들은 운영관리(admin)만 쓰고 사용자 페이지로는 들어올 수 없다. */
    private static final java.util.Set<String> OFFLINE_MANAGER_ROLES =
            java.util.Set.of("ROLE_ADMIN_7", "ROLE_ADMIN_8");

    /**
     * 오프라인 담당자(주담당자 ROLE_ADMIN_7 / 부담당자 ROLE_ADMIN_8)의 사용자 페이지 로그인을
     * 막는다 - AS-IS `AuthController:1204~1212`의 `OFF_ACCESS_FRONT`.
     *
     * <p>AS-IS는 이 검사를 비밀번호 확인 <b>전에</b> 수행해서, 비밀번호를 모르는 사람도 응답만
     * 보고 "그 아이디가 오프라인 담당자다"를 알아낼 수 있다. 차단 결과는 같으므로 여기서는
     * 비밀번호가 맞은 뒤에 검사한다 - 정상 이용자가 겪는 동작은 동일하다.
     */
    private void rejectOfflineManager(User user, String remoteAddr) {
        List<String> roles = rolesOf(user.getUserId());
        if (roles.stream().noneMatch(OFFLINE_MANAGER_ROLES::contains)) {
            return;
        }
        recordLoginLog(user.getLoginId(), "N", remoteAddr, "오프라인 담당자 계정의 사용자 페이지 접근");
        throw new MemberException("오프라인 담당자 계정은 사용자 페이지를 이용할 수 없습니다. 운영관리에서 로그인해 주세요.");
    }

    /** 2단계: 로그인 확정(횟수/최종로그인일시 갱신+성공 로그) - MFA 없는 회원은 1단계 직후,
     *  MFA 회원은 인증번호 확인 후 호출된다. */
    private User completeLogin(User user, String remoteAddr) {
        user.setLoginFailCount(0);
        user.setLoginCount((user.getLoginCount() == null ? 0 : user.getLoginCount()) + 1);
        user.setLoginDate(now());
        User saved = userRepository.save(user);
        recordLoginLog(user.getLoginId(), "Y", remoteAddr, null);
        return saved;
    }

    /** MFA 설정 화면 - 회원이 스스로 켜고 끈다. */
    @Transactional
    public void setMfaEnabled(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MemberException("회원 정보를 찾을 수 없습니다."));
        user.setMfaEnabled(enabled ? "Y" : "N");
        userRepository.save(user);
    }

    /**
     * 아이디·비밀번호 찾기 (AS-IS users/find-idpw.html 재현) - AS-IS는 이름/이메일 같은 단순
     * 조회가 아니라 실제 본인인증(휴대폰 인증 또는 금융인증서 전자서명)을 통과해야 결과를
     * 보여준다. 이 프로젝트엔 휴대폰 CI인증 게이트웨이가 없어([[external-integrations-architecture]]와
     * 동일한 이유), 휴대폰 인증번호 발송을 {@link NotificationClient}(enabled=false면 모크)로
     * 대체하되 검증 로직 자체는 실제로 동작한다 - 이름+휴대폰번호가 실제로 일치해야 인증번호가
     * 발급되고, 그 인증번호를 맞혀야만 통과한다.
     */
    public record PhoneVerificationStart(Long userId, String maskedPhone, String code, String devCode) {
    }

    /** 아이디 찾기 1단계 - 이름+휴대폰번호로 본인을 특정하고 인증번호를 발송한다. */
    public PhoneVerificationStart startFindId(String userName, String phoneNumber) {
        return sendPhoneVerification(userIdByNameAndPhone(userName, phoneNumber), phoneNumber);
    }

    /** 비밀번호 찾기 1단계 - 아이디+이름+휴대폰번호가 전부 일치해야 인증번호를 발송한다. */
    public PhoneVerificationStart startResetPassword(String loginId, String userName, String phoneNumber) {
        User user = userRepository.findByLoginId(loginId)
                .filter(u -> userName.equals(u.getUserName()))
                .orElseThrow(() -> new MemberException("일치하는 회원 정보를 찾을 수 없습니다."));
        if (!user.getUserId().equals(userIdByNameAndPhone(userName, phoneNumber))) {
            throw new MemberException("일치하는 회원 정보를 찾을 수 없습니다.");
        }
        return sendPhoneVerification(user.getUserId(), phoneNumber);
    }

    /**
     * 인증번호 단계를 건너뛰는 본인확인 - 이름+휴대폰번호가 일치하는 회원의 userId만 돌려준다.
     * 우회 스위치가 켜진 환경에서만 호출된다({@link DevBypassSettings}).
     */
    public Long verifyIdentityByNameAndPhone(String userName, String phoneNumber) {
        return userIdByNameAndPhone(userName, phoneNumber);
    }

    /** 비밀번호 찾기용 - 아이디까지 포함해 startResetPassword와 같은 검사를 하고 발송만 생략한다. */
    public Long verifyIdentityForReset(String loginId, String userName, String phoneNumber) {
        User user = userRepository.findByLoginId(loginId)
                .filter(u -> userName.equals(u.getUserName()))
                .orElseThrow(() -> new MemberException("일치하는 회원 정보를 찾을 수 없습니다."));
        if (!user.getUserId().equals(userIdByNameAndPhone(userName, phoneNumber))) {
            throw new MemberException("일치하는 회원 정보를 찾을 수 없습니다.");
        }
        return user.getUserId();
    }

    /** 입력/저장 형식 차이(하이픈·공백)를 없애고 숫자만 남긴다 - 조회 비교 기준. */
    private String phoneDigits(String phoneNumber) {
        return phoneNumber == null ? "" : phoneNumber.replaceAll("[^0-9]", "");
    }

    private Long userIdByNameAndPhone(String userName, String phoneNumber) {
        return userDetailRepository.findByPhoneNumberDigits(phoneDigits(phoneNumber)).stream()
                .filter(d -> userRepository.findById(d.getUserId())
                        .map(u -> userName.equals(u.getUserName())).orElse(false))
                .map(UserDetail::getUserId)
                .findFirst()
                .orElseThrow(() -> new MemberException("일치하는 회원 정보를 찾을 수 없습니다."));
    }

    private PhoneVerificationStart sendPhoneVerification(Long userId, String phoneNumber) {
        String code = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        notificationClient.sendAlimtalk(phoneNumber, "IDENTITY_VERIFICATION", Map.of("code", code));
        return new PhoneVerificationStart(userId, maskPhone(phoneNumber), code, notificationClient.enabled ? null : code);
    }

    /** 아이디 찾기 2단계(인증 통과 후) - AS-IS는 본인 확인이 끝난 소유자 본인에게는 마스킹 없이
     *  전체 아이디를 보여준다([[admin-pii-display-no-masking]]과 같은 맥락: 마스킹은 제3자에게
     *  노출될 수 있는 화면에서만 필요하고, 본인 인증을 마친 본인 조회는 아니다). */
    public String loginIdOf(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new MemberException("회원 정보를 찾을 수 없습니다."))
                .getLoginId();
    }

    /** 비밀번호 찾기 2단계(인증 통과 후) - AS-IS처럼 임시비밀번호를 보여주지 않고, 본인이 바로
     *  새 비밀번호를 정한다. 현재 비밀번호를 모르는 상태이므로 changePassword()와 달리
     *  현재 비밀번호 검증은 없다. */
    @Transactional
    public void resetPasswordVerified(Long userId, String newPassword, String newPasswordConfirm) {
        if (!newPassword.equals(newPasswordConfirm)) {
            throw new MemberException("새 비밀번호와 새 비밀번호 확인이 일치하지 않습니다.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MemberException("회원 정보를 찾을 수 없습니다."));
        validatePasswordComplexity(newPassword, user.getLoginId());

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setLoginFailCount(0);
        stampPasswordChanged(user);
        if (STATUS_LOCKED.equals(user.getStatusCode())) {
            user.setStatusCode(STATUS_ACTIVE);
        }
        user.setUpdatedDate(now());
        userRepository.save(user);
        recordChangeLog(userId, "PASSWORD_RESET", null);
    }

    public User byId(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new MemberException("회원 정보를 찾을 수 없습니다."));
    }

    private String maskPhone(String phoneNumber) {
        if (phoneNumber == null || !phoneNumber.matches("\\d{2,3}-?\\d{3,4}-?\\d{4}")) {
            return "***";
        }
        return phoneNumber.replaceAll("(\\d{2,3})-?\\d{3,4}-?(\\d{4})", "$1-****-$2");
    }

    /** 마이페이지 "비밀번호 변경"의 1단계 "인증" 버튼 - AS-IS checkPresentPwd()/
     *  /api/user/confirmPresentPassword 재현. 새 비밀번호 입력칸은 이 확인이 통과해야 열린다. */
    public boolean verifyPassword(Long userId, String rawPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MemberException("회원 정보를 찾을 수 없습니다."));
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword,
                                String newPasswordConfirm, String remoteAddr) {
        if (!newPassword.equals(newPasswordConfirm)) {
            throw new MemberException("새 비밀번호와 새 비밀번호 확인이 일치하지 않습니다.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MemberException("회원 정보를 찾을 수 없습니다."));
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new MemberException("현재 비밀번호가 올바르지 않습니다.");
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new MemberException("현재 비밀번호와 다른 비밀번호를 입력해 주세요.");
        }
        validatePasswordComplexity(newPassword, user.getLoginId());

        user.setPassword(passwordEncoder.encode(newPassword));
        stampPasswordChanged(user);
        user.setUpdatedDate(now());
        userRepository.save(user);
        recordChangeLog(userId, "PASSWORD_CHANGE", remoteAddr);
    }

    /** AS-IS users/modify.html의 비밀번호 변경 규칙(checkPwd/isContinued) 서버측 재검증 -
     *  화면의 실시간 체크리스트는 어디까지나 안내용이라, 우회해서 보낸 요청도 여기서 막는다. */
    private void validatePasswordComplexity(String password, String loginId) {
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasSymbol = password.matches(".*[\\{\\}\\[\\]/?.,;:|)*~`!^\\-_+<>@#$%&\\\\=('\"].*");
        if (!hasDigit || !hasLetter || !hasSymbol) {
            throw new MemberException("비밀번호는 숫자, 영문자, 기호를 모두 포함해야 합니다.");
        }
        if (password.length() < 9 || password.length() > 20) {
            throw new MemberException("비밀번호는 9자 이상 20자 이하로 입력해 주세요.");
        }
        if (hasRepeatedOrSequentialChars(password)) {
            throw new MemberException("비밀번호에 3개 이상 반복되거나 연속된 문자/숫자는 사용할 수 없습니다.");
        }
        if (!loginId.isBlank() && password.contains(loginId)) {
            throw new MemberException("비밀번호에 아이디를 포함할 수 없습니다.");
        }
    }

    private boolean hasRepeatedOrSequentialChars(String s) {
        for (int i = 0; i < s.length() - 2; i++) {
            char a = s.charAt(i), b = s.charAt(i + 1), c = s.charAt(i + 2);
            if (a == b && b == c) {
                return true;
            }
            if (b - a == 1 && c - b == 1) {
                return true;
            }
            if (b - a == -1 && c - b == -1) {
                return true;
            }
        }
        return false;
    }

    /** SessionRehydrateInterceptor가 GH_AUTH JWT로 HttpSession을 다시 채울 때 사용. */
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    /** 마이페이지 "회원 정보 수정" (AS-IS users/modify.html) 조회. */
    public UserDetail profileOf(Long userId) {
        return userDetailRepository.findById(userId).orElseGet(() -> {
            UserDetail empty = new UserDetail();
            empty.setUserId(userId);
            return empty;
        });
    }

    /**
     * 마이페이지 "회원 정보 수정" 저장. AS-IS는 이름/생년월일을 본인인증 연계로만
     * 바꿀 수 있어 읽기전용이고, 휴대폰도 인증서 팝업을 거쳐야 하지만 이 MSA는 그
     * 외부연동이 firewall 미개방 모크 상태라 평문 입력으로 대신한다(다른 외부연동과
     * 동일한 의도적 축소 - 이메일/우편번호+주소/알림동의만 실제로 이 화면에서 바뀐다).
     */
    @Transactional
    public void updateProfile(Long userId, String phoneNumber, String email, String post, String address,
                               String addressDetail, boolean receivePbanc, boolean receiveEmail,
                               boolean receiveSms, boolean receiveKakao, String remoteAddr) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MemberException("회원 정보를 찾을 수 없습니다."));
        user.setEmail(email);
        user.setUpdatedDate(now());
        userRepository.save(user);

        UserDetail detail = userDetailRepository.findById(userId).orElseGet(() -> {
            UserDetail d = new UserDetail();
            d.setUserId(userId);
            d.setLevelId(1);
            return d;
        });
        detail.setPhoneNumber(phoneNumber);
        detail.setPost(post);
        detail.setAddress(address);
        detail.setAddressDetail(addressDetail);
        detail.setReceivePbanc(receivePbanc ? "Y" : "N");
        detail.setReceiveEmail(receiveEmail ? "Y" : "N");
        detail.setReceiveSms(receiveSms ? "Y" : "N");
        detail.setReceiveKakao(receiveKakao ? "Y" : "N");
        if (detail.getUseFlag() == null) {
            detail.setUseFlag("Y");
        }
        userDetailRepository.save(detail);

        recordChangeLog(userId, "PROFILE_UPDATE", remoteAddr);
    }

    /**
     * 마이페이지 "회원 정보 수정 > 개인정보 수정"(이름/생년월일) 저장. AS-IS는 Siren24 PCC
     * 본인인증 팝업을 통과한 뒤 인증기관이 돌려준 실명/생년월일로만 바뀌는 값이라, 이 MSA는
     * 그 연계가 없는 동안 화면 자체를 열지 않는 것이 기본이다. 이 메서드는 본인인증 우회
     * 스위치가 켜진 환경에서만 호출된다({@link DevBypassSettings}) - 검증되지 않은 입력으로
     * 실명이 바뀌는 경로이므로 호출측(ProfileController)이 그 플래그를 먼저 확인해야 한다.
     */
    @Transactional
    public void updatePersonalInfo(Long userId, String userName, String birthday, String remoteAddr) {
        if (userName == null || userName.isBlank()) {
            throw new MemberException("이름을 입력해주세요.");
        }
        // 화면은 <input type="date">(yyyy-MM-dd)로 받지만 UserDetail.BIRTHDAY는 가입/현장가입과
        // 같은 yyyyMMdd 8자리로 저장한다.
        String normalizedBirthday = birthday == null ? "" : birthday.replace("-", "").trim();
        if (!normalizedBirthday.matches("[0-9]{8}")) {
            throw new MemberException("생년월일을 정확히 입력해주세요.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MemberException("회원 정보를 찾을 수 없습니다."));
        user.setUserName(userName.trim());
        user.setUpdatedDate(now());
        userRepository.save(user);

        UserDetail detail = userDetailRepository.findById(userId).orElseGet(() -> {
            UserDetail d = new UserDetail();
            d.setUserId(userId);
            d.setLevelId(1);
            d.setUseFlag("Y");
            return d;
        });
        detail.setBirthday(normalizedBirthday);
        userDetailRepository.save(detail);

        // 실명·생년월일은 본인확인 대상 정보라 PROFILE_UPDATE와 구분되는 파라미터로 남긴다.
        recordChangeLog(userId, "PERSONAL_INFO_UPDATE", remoteAddr);
    }

    @Transactional
    /**
     * 이 회원의 CI로 <b>올해 탈퇴하며 남긴 기부액 합계</b> (AS-IS `getGMberSecsnSumCntrAmt`).
     * donation의 연간 한도 계산이 잔여한도에서 빼는 값이다. CI가 없으면 이어붙일 근거가
     * 없으므로 0.
     */
    public long withdrawnDonationCarryOverOf(Long userId) {
        return userRepository.findById(userId)
                .map(User::getMberCi)
                .filter(ci -> ci != null && !ci.isBlank())
                .map(ci -> mberSecsnRepository.sumCntrAmtByCiAndYear(
                        ci, String.valueOf(LocalDate.now().getYear())))
                .orElse(0L);
    }

    /**
     * 탈퇴 시 그 해 기부액을 CI 기준으로 스냅샷해 둔다 (AS-IS `G_MBER_SECSN`).
     *
     * <p>탈퇴하면 `USER_ID`로 잡히던 올해 기부 누계가 끊겨, 재가입 후 연간 한도를 처음부터
     * 다시 쓸 수 있게 된다. AS-IS는 이 표를 CI로 조회해 잔여한도에서 빼는 방식으로 막는다
     * (`DonationVerification.isDonationNormalAmount`). 그 표를 채우는 쪽이 여기다.
     *
     * <p>CI가 없는 회원(본인인증 연계가 열리기 전 가입분)은 이어붙일 키가 없으므로 건너뛴다.
     * donation 조회가 실패해도 탈퇴 자체는 막지 않는다 - 다만 그때는 한도가 이어지지 않으므로
     * 경고를 남긴다.
     */
    void snapshotDonationForLimitCarryOver(User user) {
        String mberCi = user.getMberCi();
        if (mberCi == null || mberCi.isBlank()) {
            return;
        }
        try {
            java.math.BigDecimal thisYear = donationClient.mySummary(user.getUserId()).thisYearAmt();
            if (thisYear == null || thisYear.signum() <= 0) {
                return;
            }
            String year = String.valueOf(LocalDate.now().getYear());
            mberSecsnRepository.save(new com.ghlove.member.domain.MberSecsn(
                    year, user.getUserId(), mberCi, thisYear.intValue()));
        } catch (RuntimeException e) {
            log.warn("탈퇴 회원의 올해 기부액 스냅샷 실패 - 연간 한도가 재가입 시 이어지지 않는다. userId={}",
                    user.getUserId(), e);
        }
    }

    @Transactional
    public void withdraw(Long userId, String password, String leaveCode, String reason, String remoteAddr) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MemberException("회원 정보를 찾을 수 없습니다."));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new MemberException("비밀번호가 올바르지 않습니다.");
        }

        // 크로스 서비스 처리(포인트 소멸·관심답례품/관심지자체 삭제)를 개인정보 삭제보다 먼저
        // 한다 - 이들이 실패하면 아직 개인정보가 안 지워진 상태에서 예외로 중단되어 안전하게
        // 재시도할 수 있다(MSA는 서비스 경계를 넘는 롤백이 불가하므로 순서로 안전성을 보장).
        // mberCi를 지우기 전에 스냅샷을 먼저 떠야 한다(연간 한도 이어붙임 근거).
        snapshotDonationForLimitCarryOver(user);
        pointClient.expireAllOnWithdrawal(userId);
        giftClient.deleteWishlistOnWithdrawal(userId);          // AS-IS 관심답례품 삭제
        donationClient.deleteInterestLocgovOnWithdrawal(userId); // AS-IS 관심지자체 삭제

        // AS-IS insertSecedeCustomer 재현 - MBER_CI를 NULL로 지우기 전에 OP_USER_CI로 백업한다
        // (탈퇴 이후에도 CI 기반 조회가 가능하도록). 중복 탈퇴는 상위에서 막히므로 PK 충돌 없음.
        userCiRepository.save(new com.ghlove.member.domain.UserCi(userId, user.getMberCi()));

        // AS-IS updateSecedeGeneralCustomer(generalcustomer-mapper) 재현 - 탈퇴 즉시 OP_USER의
        // 개인식별정보를 NULL로 지운다(30일 후 배치 파기가 아니라 탈퇴 시점에 바로). LOGIN_ID는
        // 남긴다(탈퇴한 아이디 재가입 방지). 재로그인이 불가하도록 PASSWORD도 함께 지운다.
        user.setStatusCode(STATUS_WITHDRAWN);
        user.setLeaveDate(now());
        user.setPassword(null);
        user.setUserName(null);
        user.setEmail(null);
        user.setMberCi(null);
        user.setMberDi(null);
        user.setMberDn(null);
        user.setKakaoUserKey(null);
        user.setLoginCount(null);
        user.setLoginDate(null);
        user.setLoginFailCount(0);
        user.setLoginTryDate(null);
        user.setPasswordType("N");
        user.setPasswordExpiredDate(null);
        user.setSbscrbSeCode(null);
        user.setLoginPathCode(null);
        user.setUpdatedDate(now());
        userRepository.save(user);

        // AS-IS updateSecedeGeneralCustomerDetail 재현 - OP_USER_DETAIL의 개인정보도 즉시 NULL.
        userDetailRepository.findById(userId).ifPresent(detail -> {
            detail.setPhoneNumber(null);
            detail.setAddress(null);
            detail.setAddressDetail(null);
            detail.setGender(null);
            detail.setBirthday(null);
            detail.setPost(null);
            detail.setReceiveEmail(null);
            detail.setReceiveSms(null);
            detail.setReceiveKakao(null);
            detail.setReceivePbanc(null);
            detail.setLevelId(0);
            detail.setUseFlag("N");
            detail.setLeaveCode(leaveCode);
            detail.setLeaveReason(reason);
            userDetailRepository.save(detail);
        });

        // AS-IS deleteSecedeUserRole 재현 - 탈퇴 회원의 권한(OP_USER_ROLE)을 삭제한다.
        userRoleRepository.deleteByUserId(userId);

        recordChangeLog(userId, "WITHDRAW" + (reason == null || reason.isBlank() ? "" : ": " + reason), remoteAddr);
    }

    /**
     * 휴면 전환 배치 (SFR-002). 실시간 스케줄러가 없어 운영자가 수동으로 트리거한다.
     * 최근 로그인일이 기준일(SYSTEM_CONFIG/DORMANT_INACTIVE_DAYS, 기본 365일)보다
     * 오래된 ACTIVE 회원만 대상 - 한 번도 로그인하지 않은 회원(LOGIN_DATE NULL)은
     * 가입일 기준으로 판단할 근거가 마땅치 않아 이 배치에서는 제외한다.
     *
     * @return 휴면 전환된 회원 수
     */
    @Transactional
    public int runDormancyBatch() {
        String cutoff = DATE_FORMAT.format(LocalDateTime.now().minusDays(dormantInactiveDays()));
        List<User> targets = userRepository.findByStatusCodeAndLoginDateBefore(STATUS_ACTIVE, cutoff);
        for (User user : targets) {
            user.setStatusCode(STATUS_DORMANT);
            user.setUpdatedDate(now());
            userRepository.save(user);
            recordChangeLog(user.getUserId(), "DORMANT_CONVERTED (마지막 로그인: " + user.getLoginDate() + ")", null);
        }
        return targets.size();
    }

    /**
     * 휴면 해제 (AS-IS UserController.wakeup-user). AS-IS는 로그인 단계(op.saleson.js:1169)에서
     * 아이디/비밀번호로 이미 본인확인을 마친 뒤 SLEEP_USER 응답의 토큰으로 recovery를 호출하므로,
     * 여기서는 자격증명을 다시 받지 않고 로그인 때 확인된 userId로 해제한다. 로그인 응답에서
     * 넘어온 세션의 대기 userId만 이 메서드로 들어온다(AuthApiController.recovery).
     */
    @Transactional
    public User reactivateById(Long userId, String remoteAddr) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MemberException("회원 정보를 찾을 수 없습니다."));
        if (!STATUS_DORMANT.equals(user.getStatusCode())) {
            throw new MemberException("휴면 상태인 계정만 해제할 수 있습니다.");
        }
        user.setStatusCode(STATUS_ACTIVE);
        user.setUpdatedDate(now());
        User saved = userRepository.save(user);
        recordChangeLog(userId, "DORMANT_RELEASED", remoteAddr);
        return saved;
    }

    private static final String DESTROYED_NAME_MARKER = "탈퇴회원(파기됨)";
    private static final String ANONYMIZED_IP_MARKER = "0.0.0.0";
    private static final int DEFAULT_WITHDRAWN_DATA_RETENTION_DAYS = 30;
    private static final int DEFAULT_LOGIN_LOG_RETENTION_DAYS = 365;

    /**
     * SFR-002 "데이터 파기 절차" 1/2 - 탈퇴 후 유예기간(기본 30일, 분쟁/이의제기 대비 최소
     * 보관)이 지난 회원의 개인식별정보를 익명화한다. 행 자체는 지우지 않는다(donation/point/
     * order 등 다른 서비스가 userId FK로 참조 중이라 실제 삭제는 서비스 경계를 넘는 정합성
     * 문제를 일으킨다) - 이름/이메일/전화/주소/본인인증CI·DI 등 식별정보만 지운다. loginId는
     * 남겨둔다(재가입 방지 목적으로 유지하는 게 흔한 실무 관행). 매 실행마다 대상 전체를
     * 처리하고 이력을 남긴다 - 실시간 스케줄러가 없어 운영자가 수동으로 트리거한다(다른
     * batch/* 화면들과 동일한 패턴).
     */
    @Transactional
    public int purgeWithdrawnUserData() {
        String cutoff = DATE_FORMAT.format(LocalDateTime.now().minusDays(withdrawnDataRetentionDays()));
        List<User> targets = userRepository.findByStatusCodeAndLeaveDateBeforeAndUserNameNot(
                STATUS_WITHDRAWN, cutoff, DESTROYED_NAME_MARKER);
        for (User user : targets) {
            user.setUserName(DESTROYED_NAME_MARKER);
            user.setEmail(null);
            user.setMberCi(null);
            user.setMberDi(null);
            user.setMberDn(null);
            user.setUpdatedDate(now());
            userRepository.save(user);

            userDetailRepository.findById(user.getUserId()).ifPresent(detail -> {
                detail.setPhoneNumber(null);
                detail.setAddress(null);
                detail.setAddressDetail(null);
                detail.setBirthday(null);
                detail.setGender(null);
                userDetailRepository.save(detail);
            });

            UserDataDestructionLog log = new UserDataDestructionLog();
            log.setUserId(user.getUserId());
            log.setDestroyedFields("userName,email,mberCi,mberDi,mberDn,phoneNumber,address,addressDetail,birthday,gender");
            log.setReason("탈퇴 후 보관기간(" + withdrawnDataRetentionDays() + "일) 경과");
            log.setDestroyedDate(now());
            userDataDestructionLogRepository.save(log);
        }
        return targets.size();
    }

    /**
     * SFR-002 "데이터 파기 절차" 2/2 - "로그 데이터 분리 보관(익명화)". 로그인 로그는 이미
     * OP_USER_LOGIN_LOG로 회원 PII와 물리적으로 분리 보관되고 있으니, 여기서는 보관기간
     * (기본 1년)이 지난 로그의 접속 IP를 마스킹한다 - 통계/감사 목적(성공/실패 건수, 시각)은
     * 유지하면서 개인 식별에 쓰일 수 있는 IP만 지운다.
     */
    @Transactional
    public int anonymizeOldLoginLogs() {
        String cutoff = DATE_FORMAT.format(LocalDateTime.now().minusDays(loginLogRetentionDays()));
        List<LoginLog> targets = loginLogRepository.findByLoginDateBeforeAndRemoteAddrNot(cutoff, ANONYMIZED_IP_MARKER);
        for (LoginLog log : targets) {
            log.setRemoteAddr(ANONYMIZED_IP_MARKER);
            loginLogRepository.save(log);
        }
        if (!targets.isEmpty()) {
            UserDataDestructionLog historyEntry = new UserDataDestructionLog();
            historyEntry.setUserId(0L);
            historyEntry.setDestroyedFields("OP_USER_LOGIN_LOG.REMOTE_ADDR x " + targets.size() + "건");
            historyEntry.setReason("로그 보관기간(" + loginLogRetentionDays() + "일) 경과");
            historyEntry.setDestroyedDate(now());
            userDataDestructionLogRepository.save(historyEntry);
        }
        return targets.size();
    }

    public List<UserDataDestructionLog> destructionHistory() {
        return userDataDestructionLogRepository.findTop200ByOrderByDestructionIdDesc();
    }

    private int withdrawnDataRetentionDays() {
        return commonCodeRepository.findById(new com.ghlove.member.domain.CommonCodeId("SYSTEM_CONFIG", "ko", "WITHDRAWN_DATA_RETENTION_DAYS"))
                .map(com.ghlove.member.domain.CommonCode::getCodeValue)
                .filter(v -> v != null && !v.isBlank())
                .map(Integer::parseInt)
                .orElse(DEFAULT_WITHDRAWN_DATA_RETENTION_DAYS);
    }

    private int loginLogRetentionDays() {
        return commonCodeRepository.findById(new com.ghlove.member.domain.CommonCodeId("SYSTEM_CONFIG", "ko", "LOGIN_LOG_RETENTION_DAYS"))
                .map(com.ghlove.member.domain.CommonCode::getCodeValue)
                .filter(v -> v != null && !v.isBlank())
                .map(Integer::parseInt)
                .orElse(DEFAULT_LOGIN_LOG_RETENTION_DAYS);
    }

    private int dormantInactiveDays() {
        return commonCodeRepository.findById(new com.ghlove.member.domain.CommonCodeId("SYSTEM_CONFIG", "ko", "DORMANT_INACTIVE_DAYS"))
                .map(com.ghlove.member.domain.CommonCode::getCodeValue)
                .filter(v -> v != null && !v.isBlank())
                .map(Integer::parseInt)
                .orElse(DEFAULT_DORMANT_INACTIVE_DAYS);
    }

    private String statusBlockedMessage(String statusCode) {
        String label = codesOf("USER_STATUS").getOrDefault(statusCode, statusCode);
        return "이용할 수 없는 계정입니다. (상태: " + label + ")";
    }

    private void recordLoginLog(String loginId, String successFlag, String remoteAddr, String memo) {
        LoginLog log = new LoginLog();
        log.setLoginType(LOGIN_PATH_IDPW);
        log.setLoginId(loginId);
        log.setSuccessFlag(successFlag);
        log.setRemoteAddr(remoteAddr);
        log.setMemo(memo);
        log.setLoginDate(now());
        loginLogRepository.save(log);
    }

    private void recordChangeLog(Long userId, String parameter, String remoteAddr) {
        UserChangeLog log = new UserChangeLog();
        log.setUserId(userId);
        log.setParameter(parameter);
        log.setRemoteAddr(remoteAddr);
        log.setManagerId(userId);
        log.setCreatedDate(now());
        userChangeLogRepository.save(log);
    }

    private static String now() {
        return DATE_FORMAT.format(LocalDateTime.now());
    }


    /**
     * 비밀번호 유효기간 만료일 계산 - AS-IS `UserServiceImpl.getPasswordExpiredDate()`와 같이
     * "오늘 + LIFE_TIME_PASSWORD일"이다. 주기는 하드코딩하지 않고 공통코드에서 읽으며,
     * 설정이 없으면 AS-IS의 폴백과 같은 180일을 쓴다.
     */
    private String newPasswordExpiredDate() {
        return LocalDate.now().plusDays(passwordLifeTimeDays())
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    private long passwordLifeTimeDays() {
        return commonCodeRepository.findById(new com.ghlove.member.domain.CommonCodeId("SYSTEM_CONFIG", "ko", "LIFE_TIME_PASSWORD"))
                .map(com.ghlove.member.domain.CommonCode::getCodeValue)
                .map(String::trim)
                .filter(v -> v.matches("[0-9]+"))
                .map(Long::parseLong)
                .orElse(DEFAULT_PASSWORD_LIFE_TIME_DAYS);
    }

    /** 비밀번호를 새로 정한 시점에 만료일을 갱신하고 임시비밀번호 표시를 해제한다. */
    private void stampPasswordChanged(User user) {
        user.setPasswordExpiredDate(newPasswordExpiredDate());
        user.setPasswordType(PASSWORD_TYPE_NORMAL);
    }

    /**
     * 로그인 시점의 비밀번호 변경 필요 여부 (SFR-002 "인증토큰/세션 관리 - 만료·재인증 정책").
     * 만료일이 지났거나 임시비밀번호('T')면 변경을 안내한다. 만료일이 비어 있는 회원(이 정책
     * 도입 이전 가입자)은 막지 않는다 - 다음 비밀번호 변경 때 자연스럽게 채워진다.
     */
    public boolean passwordChangeRequired(User user) {
        // SNS 회원은 비밀번호로 로그인하지 않으므로 만료 대상이 아니다(AS-IS는 P를 만나면
        // 만료·임시 검사 자체를 건너뛴다 - JwtTokenAuthenticationFilter:541~545).
        if (PASSWORD_TYPE_SNS.equals(user.getPasswordType())) {
            return false;
        }
        if (PASSWORD_TYPE_TEMPORARY.equals(user.getPasswordType())) {
            return true;
        }
        String expired = user.getPasswordExpiredDate();
        if (expired == null || !expired.matches("[0-9]{8}")) {
            return false;
        }
        return expired.compareTo(LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))) < 0;
    }

    /**
     * "나중에 변경" - AS-IS `changeUserPasswordLater`. 비밀번호는 그대로 두고 만료일만
     * 다음 주기로 미룬다(임시비밀번호는 미룰 수 없다 - 반드시 바꿔야 하는 상태다).
     */
    @Transactional
    public void postponePasswordChange(Long userId, String remoteAddr) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MemberException("회원 정보를 찾을 수 없습니다."));
        if (PASSWORD_TYPE_TEMPORARY.equals(user.getPasswordType())) {
            throw new MemberException("임시 비밀번호는 반드시 변경해야 합니다.");
        }
        user.setPasswordExpiredDate(newPasswordExpiredDate());
        user.setUpdatedDate(now());
        userRepository.save(user);
        recordChangeLog(userId, "PASSWORD_CHANGE_POSTPONED", remoteAddr);
    }

    /**
     * 로그인 응답 코드 - 비밀번호 변경 필요 사유(AS-IS op.saleson.js:1182/1201). null=정상.
     * SNS('P')는 비밀번호 로그인이 아니라 제외, 임시('T')는 PASSWORD_TEMP, 만료일 경과는 PASSWORD_EXPIRED.
     * {@link #passwordChangeRequired}와 같은 규칙을 코드 문자열로 돌려준다.
     */
    public String passwordChangeCode(User user) {
        if (PASSWORD_TYPE_SNS.equals(user.getPasswordType())) {
            return null;
        }
        if (PASSWORD_TYPE_TEMPORARY.equals(user.getPasswordType())) {
            return "PASSWORD_TEMP";
        }
        String expired = user.getPasswordExpiredDate();
        if (expired != null && expired.matches("[0-9]{8}")
                && expired.compareTo(LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))) < 0) {
            return "PASSWORD_EXPIRED";
        }
        return null;
    }

    /**
     * 로그인 시점 비밀번호 변경(만료/임시 대응) - AS-IS pwdChangeModal(PASSWORD_EXPIRED) 흐름.
     * 로그인 단계에서 아이디/비번으로 본인확인을 마친 뒤 새 비밀번호를 정하고 로그인을 완료한다
     * (완료 시 만료일 갱신·임시표시 해제 = stampPasswordChanged). 방금 로그인에 쓴 현재 비밀번호와
     * 같은 값은 막는다(마이페이지 changePassword와 동일).
     */
    @Transactional
    public User changePasswordOnLogin(Long userId, String currentPassword, String newPassword, String newPasswordConfirm, String remoteAddr) {
        if (newPassword == null || !newPassword.equals(newPasswordConfirm)) {
            throw new MemberException("새 비밀번호와 새 비밀번호 확인이 일치하지 않습니다.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MemberException("회원 정보를 찾을 수 없습니다."));
        // AS-IS pwdChangeModal은 기존 비밀번호(userPW)를 다시 받아 검증한다. 로그인 단계에서 이미
        // 비번을 맞췄더라도 화면 충실도를 위해 재확인한다(임시비번 사용자 등 방어에도 유효).
        if (currentPassword == null || !passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new MemberException("기존 비밀번호가 올바르지 않습니다.");
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new MemberException("현재 비밀번호와 다른 비밀번호를 입력해 주세요.");
        }
        validatePasswordComplexity(newPassword, user.getLoginId());
        user.setPassword(passwordEncoder.encode(newPassword));
        stampPasswordChanged(user);
        user.setUpdatedDate(now());
        recordChangeLog(userId, "PASSWORD_CHANGE_ON_LOGIN", remoteAddr);
        return completeLogin(user, remoteAddr);
    }

    /** "나중에 변경"(만료 대응, 임시비번은 불가) - 만료일만 미루고 로그인을 완료한다(AS-IS delayChangePassword). */
    @Transactional
    public User postponePasswordChangeAndLogin(Long userId, String remoteAddr) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MemberException("회원 정보를 찾을 수 없습니다."));
        if (PASSWORD_TYPE_TEMPORARY.equals(user.getPasswordType())) {
            throw new MemberException("임시 비밀번호는 반드시 변경해야 합니다.");
        }
        user.setPasswordExpiredDate(newPasswordExpiredDate());
        user.setUpdatedDate(now());
        recordChangeLog(userId, "PASSWORD_CHANGE_POSTPONED", remoteAddr);
        return completeLogin(user, remoteAddr);
    }

    private String generateTempPassword() {
        String chars = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$%";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
