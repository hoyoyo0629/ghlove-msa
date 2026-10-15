package com.ghlove.member.service;

import com.ghlove.member.domain.User;
import com.ghlove.member.domain.UserDetail;
import com.ghlove.member.repository.UserDetailRepository;
import com.ghlove.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

/**
 * 관리자 계정과 회원 계정의 연결 - AS-IS는 관리자(OP_MANAGER)가 회원(OP_USER)과 <b>같은 USER_ID</b>를 쓴다.
 * 권한 승인 때 회원 정보를 관리자 테이블로 복사하고(managerrequest-mapper.insertManagerByUserSync), 오프라인 담당자를
 * 등록하면 회원 계정부터 만들고(PersonInChargeServiceImpl.insertOffPersonInCharge), 비밀번호·이름·연락처를 서로
 * 맞춘다. 본인인증(CI)·인증서(DN)는 회원 쪽 값을 본다. admin이 내부 API로 부른다(AdminManagerLinkApiController).
 */
@Service
@RequiredArgsConstructor
public class ManagerAccountLinkService {

    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    /** AS-IS 회원 상태 9(정상)·4(휴면) - TO-BE는 잠금도 AS-IS에선 상태 9(실패횟수만 다름)다. */
    private static final Set<String> STATUS_NORMAL_OR_DORMANT = Set.of("ACTIVE", "LOCKED", "DORMANT");

    private final UserRepository userRepository;
    private final UserDetailRepository userDetailRepository;
    private final PasswordEncoder passwordEncoder;

    /** 관리자 쪽이 복사·대조에 쓰는 회원 정보. asIsStatusCode는 AS-IS 표기(9 정상 / 4 휴면 / 3 탈퇴). */
    public record ManagerSyncInfo(Long userId, String loginId, String password, String userName, String email,
                                  String phoneNumber, String telNumber, String birthday, String statusCode,
                                  String asIsStatusCode, String passwordType, String passwordExpiredDate,
                                  String mberCi, String mberDn, String mberFinDn) {
    }

    public ManagerSyncInfo syncInfo(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        return user == null ? null : toSyncInfo(user);
    }

    public ManagerSyncInfo syncInfoByLoginId(String loginId) {
        User user = loginId == null ? null : userRepository.findByLoginId(loginId).orElse(null);
        return user == null ? null : toSyncInfo(user);
    }

    private ManagerSyncInfo toSyncInfo(User user) {
        UserDetail detail = userDetailRepository.findById(user.getUserId()).orElse(null);
        return new ManagerSyncInfo(user.getUserId(), user.getLoginId(), user.getPassword(), user.getUserName(),
                user.getEmail(), detail != null ? detail.getPhoneNumber() : null,
                detail != null ? detail.getTelNumber() : null, detail != null ? detail.getBirthday() : null,
                user.getStatusCode(), asIsStatusCode(user.getStatusCode()), user.getPasswordType(),
                user.getPasswordExpiredDate(), user.getMberCi(), user.getMberDn(), user.getMberFinDn());
    }

    private static String asIsStatusCode(String statusCode) {
        if ("DORMANT".equals(statusCode)) {
            return "4";
        }
        if ("WITHDRAWN".equals(statusCode)) {
            return "3";
        }
        return "ACTIVE".equals(statusCode) || "LOCKED".equals(statusCode) ? "9" : statusCode;
    }

    /** 관리자 계정용 회원번호 - AS-IS userMapper.selectNewUserId(관리자 직접 등록도 OP_USER 시퀀스를 쓴다). */
    @Transactional
    public Long nextUserId() {
        return userRepository.nextUserId();
    }

    public boolean loginIdExists(String loginId) {
        return loginId != null && userRepository.findByLoginId(loginId).isPresent();
    }

    /**
     * 오프라인 담당자 등록 - AS-IS insertOffPersonInCharge 1·2단계(insertUser + insertUserDetail). insertUser 규칙 그대로:
     * 상태 정상, 비밀번호 만료일 = 오늘 + 180일(getPasswordExpiredDate), 이메일이 없으면 아이디를 넣는다. 상세는 빈 행이다
     * (휴대폰은 관리자 쪽에만 들어가고, 담당자 정보 수정 때 updateUserDetailSync로 회원 상세에 맞춰진다).
     */
    @Transactional
    public Long createForManager(String loginId, String rawPassword, String userName, String email) {
        if (loginIdExists(loginId)) {
            throw new MemberException("이미 사용 중인 아이디입니다.");
        }
        User user = new User();
        user.setLoginId(loginId);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setUserName(userName);
        user.setEmail(email == null || email.isBlank() ? loginId : email);
        user.setStatusCode("ACTIVE");
        user.setPasswordType("N");
        user.setPasswordExpiredDate(LocalDate.now().plusDays(180).format(DATE));
        user.setLoginCount(0);
        user.setLoginFailCount(0);
        user.setCreatedDate(now());
        user.setUpdatedDate(now());
        User saved = userRepository.save(user);

        UserDetail detail = new UserDetail();
        detail.setUserId(saved.getUserId());
        detail.setLevelId(1);
        detail.setUseFlag("Y");
        userDetailRepository.save(detail);
        return saved.getUserId();
    }

    /**
     * 관리자 비밀번호 변경 팝업(초기 설정·초기화·3개월 변경)에서 바꾼 비밀번호를 회원에도 - AS-IS updateTempPasswordChange
     * 4단계(updatePasswordForUser) + 5단계(updateClearLoginFailCountForUser). 해시를 그대로 받는다.
     */
    @Transactional
    public boolean applyPasswordFromManager(Long userId, String passwordHash, String passwordType,
                                            String passwordExpiredDate) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }
        user.setPassword(passwordHash);
        user.setPasswordType(passwordType);
        user.setPasswordExpiredDate(passwordExpiredDate);
        user.setLoginFailCount(0);
        user.setUpdatedDate(now());
        userRepository.save(user);
        return true;
    }

    /**
     * 오프라인 담당자 수정 - AS-IS updateUserSync(이름·이메일) + updateUserDetailSync(휴대폰, 지점 연락처는 값이 있을 때만).
     */
    @Transactional
    public void syncProfileFromManager(Long userId, String userName, String email, String phoneNumber, String telNumber) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setUserName(userName);
            user.setEmail(email);
            userRepository.save(user);
        });
        userDetailRepository.findById(userId).ifPresent(detail -> {
            detail.setPhoneNumber(phoneNumber);
            if (telNumber != null && !telNumber.isEmpty()) {
                detail.setTelNumber(telNumber);
            }
            userDetailRepository.save(detail);
        });
    }

    /** 오프라인 담당자 삭제 - AS-IS deleteChargerTelNumber(지점 연락처를 빈 값으로). 회원 계정은 남는다. */
    @Transactional
    public void clearTelNumber(List<Long> userIds) {
        for (UserDetail detail : userDetailRepository.findAllById(userIds)) {
            detail.setTelNumber("");
            userDetailRepository.save(detail);
        }
    }

    /** 인증서 등록 화면 본인 확인용 회원 - AS-IS getPkiManagerFormValid/Off의 OP_USER·OP_USER_DETAIL 부분. */
    public record PkiUser(Long userId, String phoneNumber, String mberCi, String mberDn, String mberFinDn) {
    }

    /**
     * 지자체 담당자: 아이디 + 생년월일(사용 중인 상세행)로 찾는다(getPkiManagerFormValid). 오프라인 담당자: 아이디만
     * (getPkiManagerFormValidOff - 개인번호·지점 연락처는 관리자 쪽 값이라 admin이 대조한다). 둘 다 회원 상태 9·4.
     * 휴대폰은 AS-IS처럼 '-'를 뺀다.
     */
    public PkiUser pkiUser(String loginId, String birthday, boolean byBirthday) {
        User user = loginId == null ? null : userRepository.findByLoginId(loginId).orElse(null);
        if (user == null || !STATUS_NORMAL_OR_DORMANT.contains(user.getStatusCode())) {
            return null;
        }
        UserDetail detail = userDetailRepository.findById(user.getUserId()).orElse(null);
        if (byBirthday && (detail == null || !"Y".equals(detail.getUseFlag())
                || birthday == null || !birthday.equals(detail.getBirthday()))) {
            return null;
        }
        String phone = detail != null && detail.getPhoneNumber() != null ? detail.getPhoneNumber().replace("-", "") : "";
        return new PkiUser(user.getUserId(), phone, user.getMberCi(), user.getMberDn(), user.getMberFinDn());
    }

    /**
     * 인증서 등록 - AS-IS updatePkiManagerMberDn. 같은 DN을 쓰던 다른 회원에게서 먼저 지운 뒤 이 회원에 넣는다
     * (공동인증서 DN → MBER_DN, 금융인증서 DN → MBER_FIN_DN). 둘 다 비면 MBER_DN을 비운다.
     */
    @Transactional
    public String updateDn(Long userId, String mberDn, String mberFinDn) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return "FAIL";
        }
        if (mberDn != null && !mberDn.isEmpty()) {
            for (User other : userRepository.findByMberDn(mberDn)) {
                other.setMberDn("");
                other.setUpdatedDate(now());
                userRepository.save(other);
            }
            user.setMberDn(mberDn);
        } else if (mberFinDn != null && !mberFinDn.isEmpty()) {
            for (User other : userRepository.findByMberFinDn(mberFinDn)) {
                other.setMberFinDn("");
                other.setUpdatedDate(now());
                userRepository.save(other);
            }
            user.setMberFinDn(mberFinDn);
        } else {
            user.setMberDn(mberDn);
        }
        user.setUpdatedDate(now());
        userRepository.save(user);
        return "SUCC";
    }

    /** 인증서 삭제 - AS-IS updateDeletePkiManagerMberDn(종류 P/F별로 값이 있을 때만 NULL로). */
    @Transactional
    public String deleteDn(Long userId, String type, String mberDn, String mberFinDn) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return "FAIL";
        }
        String code = "FAIL";
        if ("P".equals(type) && mberDn != null && !mberDn.isEmpty()) {
            user.setMberDn(null);
            code = "SUCC";
        }
        if ("F".equals(type) && mberFinDn != null && !mberFinDn.isEmpty()) {
            user.setMberFinDn(null);
            code = "SUCC";
        }
        if ("SUCC".equals(code)) {
            user.setUpdatedDate(now());
            userRepository.save(user);
        }
        return code;
    }

    /** 인증서 로그인 - AS-IS getUserByMberDn / getUserByMberFinDn(상태 조건 없음). 공동인증서 DN이 우선이다. */
    public ManagerSyncInfo byDn(String mberDn, String mberFinDn) {
        if (mberDn != null && !mberDn.isEmpty()) {
            List<User> found = userRepository.findByMberDn(mberDn);
            if (!found.isEmpty()) {
                return toSyncInfo(found.get(0));
            }
        }
        if (mberFinDn != null && !mberFinDn.isEmpty()) {
            List<User> found = userRepository.findByMberFinDn(mberFinDn);
            if (!found.isEmpty()) {
                return toSyncInfo(found.get(0));
            }
        }
        return null;
    }

    private static String now() {
        return LocalDateTime.now().format(DATETIME);
    }
}
