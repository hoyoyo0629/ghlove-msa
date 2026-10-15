package com.ghlove.admin.service;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.domain.ManagerLoginEmail;
import com.ghlove.admin.repository.ManagerLoginEmailRepository;
import com.ghlove.admin.repository.ManagerRepository;
import com.ghlove.admin.service.integration.EmailClient;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 관리자(운영자) 로그인 - AS-IS OP_MANAGER + OP_MANAGER_LOGIN_EMAIL 기반. AS-IS(opmanager/
 * user/login_main.jsp)와 동일하게 2단계다: (1) 아이디/비번 확인만으로는 세션이 서지 않고,
 * (2) 이메일로 받은 6자리 인증번호(5분 유효)까지 맞아야 최종 로그인된다. 실패 5회 시
 * 계정 잠금은 member의 MemberService.login()과 동일한 로직.
 */
@Service
@RequiredArgsConstructor
public class ManagerAuthService {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final int MAX_LOGIN_FAIL_COUNT = 5;
    private static final int AUTH_CODE_VALID_MINUTES = 5;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ManagerRepository managerRepository;
    private final ManagerLoginEmailRepository managerLoginEmailRepository;
    private final CommonCodeService commonCodeService;
    private final PasswordEncoder passwordEncoder;
    private final EmailClient emailClient;
    private final CertLoginService certLoginService;
    private final com.ghlove.admin.repository.ManagerRequestRepository managerRequestRepository;
    private final MemberClient memberClient;
    private final com.ghlove.admin.repository.ConfigIsmsRepository configIsmsRepository;

    /** 아이디 로그인 1단계 결과 - code는 AS-IS ManagerRequestResult.code 값 그대로. */
    public record LoginValidResult(String code, Long userId) {
    }

    /** 본인인증(CI) 확인을 건너뛰는 권한 - 오프라인 주·부담당자(AS-IS getLoginUserValid의 ROLE_ADMIN_7/8). */
    private static final java.util.Set<String> CI_EXEMPT_ROLES = java.util.Set.of("ROLE_ADMIN_7", "ROLE_ADMIN_8");

    /**
     * 아이디 로그인 1단계 - AS-IS {@code ManagerRequestServiceImpl.getLoginUserValid} 이식. 결과코드도 AS-IS 그대로다:
     * <ul>
     *   <li>관리자 계정이면: 오프라인 담당자(7·8)가 아닌데 같은 회원번호 회원의 본인인증(CI)이 없으면 {@code EMPTY_CI}
     *       → 잠김(실패 5회 이상) {@code ERR_ROCK} → 비밀번호 불일치는 실패횟수 +1 후 {@code ERR_MNG_PASS}
     *       → 비밀번호 변경 대상이면 {@code PASS_CHANGE_F/I/C} → 통과 {@code SUCC}. AS-IS는 잠김을 실패횟수로만 본다
     *       (상태를 바꾸지 않는다) - 그래야 회원 "비밀번호 찾기"로 비밀번호를 바꾸면 실패횟수가 풀려 다시 들어온다.</li>
     *   <li>관리자가 아니면 일반회원인지 본다: 없음 {@code ERR_USER_NOT} / 비밀번호 불일치 {@code ERR_USER_PASS} /
     *       신청중(200) 관리자 권한 요청이 있으면 {@code REQ}, 없으면 {@code REQ_NOT}(화면이 권한 요청 폼으로 보낸다).</li>
     *   <li>조회 실패·사용 중지(TO-BE 'LOCKED' = AS-IS 상태 2) 관리자는 {@code ERR}.</li>
     * </ul>
     */
    @Transactional(noRollbackFor = ManagerException.class)
    public LoginValidResult validateLogin(String loginId, String rawPassword) {
        Manager manager = loginId == null ? null : managerRepository.findByLoginId(loginId).orElse(null);
        if (manager != null) {
            if (!CI_EXEMPT_ROLES.contains(manager.getAuthority())) {
                MemberClient.ManagerSyncInfo member = memberClient.managerSyncInfo(manager.getUserId());
                if (member == null || member.mberCi() == null || member.mberCi().isEmpty()) {
                    return new LoginValidResult("EMPTY_CI", manager.getUserId());
                }
            }
            int failCount = manager.getLoginFailCount() == null ? 0 : manager.getLoginFailCount();
            if (failCount >= MAX_LOGIN_FAIL_COUNT) {
                return new LoginValidResult("ERR_ROCK", manager.getUserId());
            }
            if (rawPassword == null || !passwordEncoder.matches(rawPassword, manager.getPassword())) {
                manager.setLoginFailCount(failCount + 1);
                manager.setLoginTryDate(now());
                managerRepository.save(manager);
                return new LoginValidResult("ERR_MNG_PASS", manager.getUserId());
            }
            if (!STATUS_ACTIVE.equals(manager.getStatusCode())) {
                // 사용 중지(AS-IS 2) - AS-IS는 인증번호 확인 뒤 세션 생성(loadUserByUsername)에서 막혀 공통 오류가 된다.
                return new LoginValidResult("ERR", manager.getUserId());
            }
            String passwordChangeType = passwordChangeType(manager);
            if (!passwordChangeType.isEmpty()) {
                return new LoginValidResult(passwordChangeType, manager.getUserId());
            }
            return new LoginValidResult("SUCC", manager.getUserId());
        }

        MemberClient.CredentialCheck member = memberClient.checkCredential(loginId, rawPassword);
        if (member == null) {
            return new LoginValidResult("ERR", null);
        }
        if ("NOT_FOUND".equals(member.result())) {
            return new LoginValidResult("ERR_USER_NOT", null);
        }
        if (!"OK".equals(member.result())) {
            return new LoginValidResult("ERR_USER_PASS", null);
        }
        boolean requested = managerRequestRepository.findByUserIdOrderByReqstSnDesc(member.userId()).stream()
                .anyMatch(r -> com.ghlove.admin.domain.ManagerRequest.STATUS_PENDING.equals(r.getConfmSttusCode()));
        return new LoginValidResult(requested ? "REQ" : "REQ_NOT", member.userId());
    }

    /** 1단계: 아이디/비번 확인. 통과해도 아직 로그인은 아니다(이메일 인증 전까지 세션 미확정). */
    @Transactional(noRollbackFor = ManagerException.class)
    public Manager checkCredentials(String loginId, String rawPassword) {
        Manager manager = managerRepository.findByLoginId(loginId).orElse(null);
        if (manager == null) {
            throw new ManagerException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        if (!STATUS_ACTIVE.equals(manager.getStatusCode())) {
            throw new ManagerException(statusBlockedMessage(manager.getStatusCode()));
        }
        if (!passwordEncoder.matches(rawPassword, manager.getPassword())) {
            int failCount = (manager.getLoginFailCount() == null ? 0 : manager.getLoginFailCount()) + 1;
            manager.setLoginFailCount(failCount);
            manager.setLoginTryDate(now());
            // AS-IS처럼 잠김은 실패횟수로만 판정한다(상태를 바꾸지 않는다 - 상태 'LOCKED'는 사용 중지와 같은 값이다).
            boolean justLocked = failCount >= MAX_LOGIN_FAIL_COUNT;
            managerRepository.save(manager);
            if (justLocked) {
                throw new ManagerException("로그인 실패 횟수를 초과하여 계정이 잠겼습니다. 시스템 관리자에게 문의하세요.");
            }
            throw new ManagerException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        return manager;
    }

    /**
     * 비밀번호 변경 유형 - AS-IS managerrequest-mapper.getPasswordChangeTypeByUserId(정상 상태 관리자만):
     * 임시 비밀번호('T')인데 정보수정일이 없으면 {@code PASS_CHANGE_F}(초기 비밀번호 설정), 임시면 {@code PASS_CHANGE_I}(초기화),
     * 만료일이 오늘 이전이거나 오늘이면({@code DATEDIFF(만료일, NOW()) < 1}) {@code PASS_CHANGE_C}(3개월 변경 안내), 아니면 빈 값.
     * 만료일이 비어 있으면 AS-IS DATEDIFF가 NULL이라 해당 없음이다.
     */
    public String passwordChangeType(Manager manager) {
        if (manager == null || !STATUS_ACTIVE.equals(manager.getStatusCode())) {
            return "";
        }
        if ("T".equals(manager.getPasswordType())) {
            return manager.getInfoUpdtDe() == null || manager.getInfoUpdtDe().isEmpty() ? "PASS_CHANGE_F" : "PASS_CHANGE_I";
        }
        String expired = manager.getPasswordExpiredDate();
        if (expired != null && expired.length() >= 8 && expired.substring(0, 8).chars().allMatch(Character::isDigit)) {
            java.time.LocalDate expiredDate = java.time.LocalDate.parse(expired.substring(0, 8),
                    DateTimeFormatter.ofPattern("yyyyMMdd"));
            if (!expiredDate.isAfter(java.time.LocalDate.now())) {
                return "PASS_CHANGE_C";
            }
        }
        return "";
    }

    /**
     * 비밀번호 변경 팝업 저장 - AS-IS {@code UserServiceImpl.updateTempPasswordChange}. 결과코드 그대로:
     * 관리자 없음 {@code ERR_NOT_FOUND} / 현재(임시) 비밀번호 불일치 {@code ERR_PASS_MATCH} / 새 비밀번호가 기존과 같음
     * {@code ERR_PASS_CHANGE} / 성공 {@code SUCC}(비밀번호 유형 'N', 만료일 = 오늘 + ISMS LIFE_TIME_PASSWORD일(없으면 180),
     * 로그인 실패횟수 초기화). 같은 회원번호의 회원 비밀번호도 같은 값으로 바꾸고 회원 실패횟수도 푼다(AS-IS 4·5단계).
     */
    @Transactional
    public String changeTempPassword(String loginId, String currentPassword, String newPassword) {
        Manager manager = loginId == null ? null : managerRepository.findByLoginId(loginId).orElse(null);
        if (manager == null) {
            return "ERR_NOT_FOUND";
        }
        if (currentPassword == null || !passwordEncoder.matches(currentPassword, manager.getPassword())) {
            return "ERR_PASS_MATCH";
        }
        if (newPassword == null || passwordEncoder.matches(newPassword, manager.getPassword())) {
            return "ERR_PASS_CHANGE";
        }
        manager.setPassword(passwordEncoder.encode(newPassword));
        manager.setPasswordType("N");
        manager.setPasswordExpiredDate(passwordExpiredDate());
        manager.setLoginFailCount(0);
        managerRepository.save(manager);
        try {
            memberClient.applyPasswordFromManager(manager.getUserId(), manager.getPassword(), "N",
                    manager.getPasswordExpiredDate());
        } catch (RuntimeException e) {
            org.slf4j.LoggerFactory.getLogger(ManagerAuthService.class)
                    .error("관리자 비밀번호 변경 - 회원 비밀번호 동기화 실패 userId={}", manager.getUserId(), e);
        }
        return "SUCC";
    }

    /**
     * 회원 비밀번호가 바뀌면(마이페이지 변경·비밀번호 찾기·로그인 시 변경) 같은 아이디의 관리자 비밀번호도 바꾸고 실패횟수를 푼다 -
     * AS-IS UserServiceImpl "관리자동기화"(getManagerByLoginId → updatePasswordForManager → updateClearLoginFailCountForManager).
     * member의 MEMBER_PASSWORD_CHANGED 이벤트로 들어온다. 관리자가 아니면 아무것도 하지 않는다.
     */
    @Transactional
    public void syncPasswordFromMember(String loginId, String passwordHash, String passwordType, String passwordExpiredDate) {
        Manager manager = loginId == null ? null : managerRepository.findByLoginId(loginId).orElse(null);
        if (manager == null || passwordHash == null) {
            return;
        }
        manager.setPassword(passwordHash);
        manager.setPasswordType(passwordType == null ? "N" : passwordType);
        manager.setPasswordExpiredDate(passwordExpiredDate);
        manager.setLoginFailCount(0);
        manager.setUpdatedDate(now());
        managerRepository.save(manager);
    }

    /** AS-IS getPasswordExpiredDate - 오늘 + ISMS 설정 LIFE_TIME_PASSWORD일(설정이 없으면 180일), yyyyMMdd. */
    private String passwordExpiredDate() {
        int days = configIsmsRepository.findById("LIFE_TIME_PASSWORD")
                .map(com.ghlove.admin.domain.ConfigIsms::getValue)
                .filter(v -> v != null && v.trim().matches("\\d+"))
                .map(v -> Integer.parseInt(v.trim()))
                .orElse(180);
        return java.time.LocalDate.now().plusDays(days).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    public Manager findByLoginId(String loginId) {
        return managerRepository.findByLoginId(loginId).orElse(null);
    }

    /** 이메일 연계가 꺼져 있으면(로컬) 인증번호를 화면에 돌려줘 자동 입력·로그인하게 한다 - AS-IS IS_LOCAL_DEV 우회. */
    public boolean emailBypass() {
        return !emailClient.enabled;
    }

    /** 2단계: 인증번호 생성+발송. EmailClient가 모크(enabled=false)면 개발 편의상 코드를 그대로 반환한다. */
    @Transactional
    public String sendAuthCode(Manager manager) {
        String code = generateCode();
        ManagerLoginEmail row = new ManagerLoginEmail();
        row.setLoginId(manager.getLoginId());
        row.setAuthNum(code);
        row.setCreatedAt(LocalDateTime.now());
        managerLoginEmailRepository.save(row);
        emailClient.sendAuthCode(manager.getEmail(), code);
        return emailClient.enabled ? null : code;
    }

    /** 이메일이 없는 계정이 최초 인증 시 이메일을 등록하면서 바로 인증번호를 받는다. */
    @Transactional
    public String saveEmailAndSendCode(String loginId, String email) {
        Manager manager = managerRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ManagerException("계정을 찾을 수 없습니다."));
        manager.setEmail(email);
        managerRepository.save(manager);
        return sendAuthCode(manager);
    }

    /** 3단계: 인증번호 확인 - 통과해야 실제 로그인(횟수/일시 갱신)이 완료된다. */
    @Transactional
    public Manager verifyAuthCode(String loginId, String inputCode) {
        ManagerLoginEmail row = managerLoginEmailRepository.findFirstByLoginIdOrderByCreatedAtDesc(loginId)
                .orElseThrow(() -> new ManagerException("인증번호를 먼저 요청해 주세요."));
        if (row.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(AUTH_CODE_VALID_MINUTES))) {
            throw new ManagerException("인증 시간이 초과되었습니다. 다시 시도해 주세요.");
        }
        if (inputCode == null || !inputCode.trim().equals(row.getAuthNum())) {
            throw new ManagerException("인증번호가 일치하지 않습니다.");
        }
        Manager manager = managerRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ManagerException("계정을 찾을 수 없습니다."));
        manager.setLoginFailCount(0);
        manager.setLoginCount((manager.getLoginCount() == null ? 0 : manager.getLoginCount()) + 1);
        manager.setLoginDate(now());
        return managerRepository.save(manager);
    }

    /** 인증서 로그인 결과 - AS-IS PkiManagerLoginResult(code, statusCode, mberDn/mberFinDn) + 성공 시 관리자. */
    public record PkiLoginResult(String code, String statusCode, String mberDn, String mberFinDn, Manager manager) {
    }

    /**
     * 인증서(공동/금융) 로그인 - AS-IS {@code UserServiceImpl.savePkiManagerLoginUserValid}. 인증서 DN은 <b>회원</b>에
     * 등록돼 있다(OP_USER.MBER_DN / MBER_FIN_DN). 결과코드 그대로:
     * DN 없음 {@code ERR_DN} / 그 DN의 회원 없음 {@code ERR_USER_NOT} / 회원은 있는데 관리자가 아님 {@code ERR_MNG_NOT}
     * (화면이 그 DN으로 권한 요청 폼을 연다) / 관리자 상태가 정상(9)이 아님 {@code ERR_STS}(+statusCode) / 성공 {@code SUCC}.
     * 인증서 자체가 2차 인증이라 이메일 인증번호는 묻지 않는다. 성공이면 로그인 처리(실패횟수 초기화)까지 한다.
     */
    @Transactional(noRollbackFor = ManagerException.class)
    public PkiLoginResult pkiLogin(String mberDn, String mberFinDn) {
        if ((mberDn == null || mberDn.isEmpty()) && (mberFinDn == null || mberFinDn.isEmpty())) {
            return new PkiLoginResult("ERR_DN", null, null, null, null);
        }
        MemberClient.ManagerSyncInfo user = memberClient.byDn(mberDn, mberFinDn);
        if (user == null) {
            return new PkiLoginResult("ERR_USER_NOT", null, null, null, null);
        }
        Manager manager = managerRepository.findByLoginId(user.loginId()).orElse(null);
        if (manager == null) {
            boolean byDn = mberDn != null && !mberDn.isEmpty() && mberDn.equals(user.mberDn());
            return new PkiLoginResult("ERR_MNG_NOT", null, byDn ? mberDn : null, byDn ? null : mberFinDn, null);
        }
        if (!STATUS_ACTIVE.equals(manager.getStatusCode())) {
            return new PkiLoginResult("ERR_STS", manager.getAsIsStatusCode(), null, null, null);
        }
        manager.setLoginFailCount(0);
        manager.setLoginCount((manager.getLoginCount() == null ? 0 : manager.getLoginCount()) + 1);
        manager.setLoginDate(now());
        return new PkiLoginResult("SUCC", null, null, null, managerRepository.save(manager));
    }

    public java.security.cert.X509Certificate parseCertificate(org.springframework.web.multipart.MultipartFile file) {
        return certLoginService.parseCertificate(file);
    }

    /** 인증서 등록 화면 본인 확인 결과 - AS-IS PkiManagerLoginResult(code, userId, mberDn, mberFinDn, mberCi). */
    public record PkiFormValidResult(String code, Long userId, String mberDn, String mberFinDn, String mberCi) {
    }

    /**
     * 인증서 등록 화면 "등록/삭제" 본인 확인 - AS-IS {@code UserServiceImpl.getPkiManagerFormValid}.
     * <ul>
     *   <li>지자체 담당자(loc): 아이디 + 생년월일로 회원(상태 9·4)을 찾는다. 휴대폰이 없으면 {@code ERR_PHONE},
     *       본인인증(CI)이 없으면 {@code ERR_CI}, 입력한 휴대폰과 같으면 {@code SUCC}.</li>
     *   <li>오프라인 담당자(off): 아이디로 회원을 찾고, 같은 회원번호의 관리자(상태 정상)가 입력한 개인번호를 가져야 한다.
     *       관리자 휴대폰(지점 연락처)이 없으면 {@code ERR_PHONE}, 입력값과 같으면 {@code SUCC}(CI 확인 없음).</li>
     *   <li>못 찾으면 {@code FAIL}(화면: "회원정보가 존재하지 않아 …").</li>
     * </ul>
     * 찾았으면 AS-IS처럼 관리자 비밀번호 유형을 'N'·만료일을 새로 맞춘다(updatePasswordExpiredDateForManager).
     */
    @Transactional
    public PkiFormValidResult pkiFormValid(String charge, String loginId, String phoneNumber, String birthday, String offEmpId) {
        String code = "FAIL";
        Long userId = null;
        MemberClient.PkiUser user = null;
        String phone = null;
        if ("loc".equals(charge)) {
            user = memberClient.pkiUser(loginId, birthday, "loc");
            phone = user != null ? user.phoneNumber() : null;
        } else if ("off".equals(charge)) {
            MemberClient.PkiUser found = memberClient.pkiUser(loginId, null, "off");
            Manager manager = found == null ? null : managerRepository.findById(found.userId()).orElse(null);
            if (manager != null && STATUS_ACTIVE.equals(manager.getStatusCode())
                    && offEmpId != null && offEmpId.equals(manager.getEmpId())) {
                user = found;
                phone = manager.getPhoneNumber() == null ? "" : manager.getPhoneNumber().replace("-", "");
            }
        }
        if (user != null) {
            userId = user.userId();
            if (phone == null || phone.isEmpty()) {
                code = "ERR_PHONE";
            } else if ("loc".equals(charge) && (user.mberCi() == null || user.mberCi().isEmpty())) {
                code = "ERR_CI";
            } else if ((phoneNumber == null ? "" : phoneNumber).equals(phone)) {
                code = "SUCC";
            }
            managerRepository.findById(userId).ifPresent(manager -> {
                manager.setPasswordType("N");
                manager.setPasswordExpiredDate(passwordExpiredDate());
                manager.setUpdatedDate(now());
                managerRepository.save(manager);
            });
        }
        return "SUCC".equals(code)
                ? new PkiFormValidResult(code, userId, user.mberDn(), user.mberFinDn(), user.mberCi())
                : new PkiFormValidResult(code, null, null, null, null);
    }

    private String statusBlockedMessage(String statusCode) {
        String label = commonCodeService.labelsOf("MANAGER_STATUS").getOrDefault(statusCode, statusCode);
        return "이용할 수 없는 계정입니다. (상태: " + label + ")";
    }

    private static String generateCode() {
        return String.format("%06d", new SecureRandom().nextInt(1_000_000));
    }

    private static String now() {
        return LocalDateTime.now().format(DATE_FORMAT);
    }
}
