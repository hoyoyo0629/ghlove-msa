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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_LOCKED = "LOCKED";
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
    public User signup(SignupForm form) {
        if (!form.getPassword().equals(form.getPasswordConfirm())) {
            throw new MemberException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }
        userRepository.findByLoginId(form.getLoginId()).ifPresent(u -> {
            throw new MemberException("이미 사용 중인 아이디입니다.");
        });

        User user = new User();
        user.setLoginId(form.getLoginId());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setUserName(form.getUserName());
        user.setEmail(form.getEmail());
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
        detail.setPhoneNumber(form.getPhoneNumber());
        // <input type=date>는 yyyy-MM-dd로 넘어오는데 BIRTHDAY 컬럼은 다른 서비스(예: donation의
        // 기부확인증)에서 yyyyMMdd로 파싱하므로 하이픈을 제거해서 저장한다.
        detail.setBirthday(form.getBirthday() != null ? form.getBirthday().replace("-", "") : null);
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
    public record LoginOutcome(User user, boolean mfaRequired, String maskedPhone, String devCode) {
    }

    public LoginOutcome loginWithMfaCheck(String loginId, String rawPassword, String remoteAddr) {
        User user = checkCredentials(loginId, rawPassword, remoteAddr);
        if (!"Y".equals(user.getMfaEnabled())) {
            return new LoginOutcome(completeLogin(user, remoteAddr), false, null, null);
        }
        UserDetail detail = userDetailRepository.findById(user.getUserId()).orElse(null);
        String phoneNumber = detail != null ? detail.getPhoneNumber() : null;
        if (phoneNumber == null || phoneNumber.isBlank()) {
            // 휴대폰번호가 없으면 MFA를 켜둔 의미가 없다 - 인증수단이 없으니 그냥 통과시킨다
            // (설정 화면에서 휴대폰번호 없이는 MFA를 켤 수 없게 막는 게 근본 해결이지만,
            // 이미 켜둔 상태에서 번호를 지운 경우까지 로그인 자체를 막아버리면 계정이 잠긴다).
            return new LoginOutcome(completeLogin(user, remoteAddr), false, null, null);
        }
        String code = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        notificationClient.sendAlimtalk(phoneNumber, "LOGIN_MFA", Map.of("code", code));
        pendingMfaCodes.put(user.getUserId(), new PendingMfaCode(code, LocalDateTime.now(), remoteAddr));
        return new LoginOutcome(user, true, maskPhone(phoneNumber), notificationClient.enabled ? null : code);
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

        if (!STATUS_ACTIVE.equals(user.getStatusCode())) {
            recordLoginLog(loginId, "N", remoteAddr, "차단된 계정 상태: " + user.getStatusCode());
            throw new MemberException(statusBlockedMessage(user.getStatusCode()));
        }

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            int failCount = (user.getLoginFailCount() == null ? 0 : user.getLoginFailCount()) + 1;
            user.setLoginFailCount(failCount);
            user.setLoginTryDate(now());

            String memo = "비밀번호 불일치";
            boolean justLocked = failCount >= MAX_LOGIN_FAIL_COUNT;
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
        return user;
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

    private Long userIdByNameAndPhone(String userName, String phoneNumber) {
        return userDetailRepository.findByPhoneNumber(phoneNumber).stream()
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
                               String addressDetail, boolean receiveEmail, boolean receiveSms,
                               boolean receiveKakao, String remoteAddr) {
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
        detail.setReceiveEmail(receiveEmail ? "Y" : "N");
        detail.setReceiveSms(receiveSms ? "Y" : "N");
        detail.setReceiveKakao(receiveKakao ? "Y" : "N");
        if (detail.getUseFlag() == null) {
            detail.setUseFlag("Y");
        }
        userDetailRepository.save(detail);

        recordChangeLog(userId, "PROFILE_UPDATE", remoteAddr);
    }

    @Transactional
    public void withdraw(Long userId, String password, String leaveCode, String reason, String remoteAddr) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MemberException("회원 정보를 찾을 수 없습니다."));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new MemberException("비밀번호가 올바르지 않습니다.");
        }

        user.setStatusCode(STATUS_WITHDRAWN);
        user.setLeaveDate(now());
        user.setUpdatedDate(now());
        userRepository.save(user);

        userDetailRepository.findById(userId).ifPresent(detail -> {
            detail.setLeaveCode(leaveCode);
            detail.setLeaveReason(reason);
            userDetailRepository.save(detail);
        });

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

    /** 휴면 해제 - 아이디/비밀번호로 본인 확인 후 즉시 정상 전환한다 (로그인 자체는 휴면 상태에서 막혀 있어 별도 절차 필요). */
    @Transactional
    public User reactivate(String loginId, String password, String remoteAddr) {
        User user = userRepository.findByLoginId(loginId).orElse(null);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new MemberException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        if (!STATUS_DORMANT.equals(user.getStatusCode())) {
            throw new MemberException("휴면 상태인 계정만 해제할 수 있습니다.");
        }
        user.setStatusCode(STATUS_ACTIVE);
        user.setUpdatedDate(now());
        User saved = userRepository.save(user);
        recordChangeLog(user.getUserId(), "DORMANT_RELEASED", remoteAddr);
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
