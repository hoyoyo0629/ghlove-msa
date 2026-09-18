package com.ghlove.member.web;

import com.ghlove.member.domain.User;
import com.ghlove.member.domain.UserChangeLog;
import com.ghlove.member.domain.UserDetail;
import com.ghlove.member.repository.LoginLogRepository;
import com.ghlove.member.repository.UserActionLogRepository;
import com.ghlove.member.repository.UserChangeLogRepository;
import com.ghlove.member.repository.UserDetailRepository;
import com.ghlove.member.repository.UserRepository;
import com.ghlove.member.service.MemberException;
import com.ghlove.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Read-only JSON API for other services (donation's 기부확인증 needs 성명/생년월일) -
 * same pattern as gift's GET /api/gifts/{id} for order. registerWalkIn()만 예외적으로
 * 쓰기 - donation의 오프라인 기부접수(offgive)가 계좌 없는 방문 시민을 즉석 가입시킬 때
 * 호출한다(admin 콘솔의 OP_MANAGER 로그인이 실제 게이트, 이 엔드포인트 자체는 브라우저에
 * 직접 노출되지 않음 - CtbnyOpratnApiController와 동일한 관행).
 */
@RestController
@RequiredArgsConstructor
public class UserApiController {

    private final UserRepository userRepository;
    private final UserDetailRepository userDetailRepository;
    private final MemberService memberService;
    private final LoginLogRepository loginLogRepository;
    private final UserActionLogRepository userActionLogRepository;
    private final UserChangeLogRepository userChangeLogRepository;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @GetMapping("/api/users/{userId}")
    public ResponseEntity<UserInfoDto> get(@PathVariable Long userId) {
        return userRepository.findById(userId)
                .map(user -> UserInfoDto.of(user, userDetailRepository.findById(userId).orElse(null)))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * 이 회원과 같은 사람(CI 동일)이 <b>올해 탈퇴하면서 남긴 기부액</b>. donation의 연간 한도
     * 계산이 잔여한도에서 이 값을 빼기 위해 호출한다 - AS-IS `DonationVerification`이
     * `getGMberSecsnSumCntrAmt(mberCi)`로 `G_MBER_SECSN`을 CI로 조회하던 것과 같은 자리다.
     * CI는 개인식별자라 응답에 담지 않고 <b>합계만</b> 돌려준다.
     */
    @GetMapping("/api/users/{userId}/withdrawn-donation-carryover")
    public Map<String, Long> withdrawnDonationCarryOver(@PathVariable Long userId) {
        return Map.of("amount", memberService.withdrawnDonationCarryOverOf(userId));
    }

    /** AS-IS join.html checkIdUsedYn()의 POST /api/join/getUserInfoByUserId 대응 - 회원가입 아이디 중복확인.
     *  AS-IS는 이 엔드포인트도 `checkDuplication()`을 그대로 태워 <b>가입 불가 아이디 목록</b>까지
     *  같이 대조하므로(JoinController:129), 금지 아이디도 "이미 사용중"으로 나온다. */
    @GetMapping("/api/check-login-id")
    public Map<String, Boolean> checkLoginId(@RequestParam String loginId) {
        return Map.of("available", memberService.loginIdAvailable(loginId));
    }

    @PostMapping("/api/users/walk-in")
    public ResponseEntity<?> registerWalkIn(@RequestParam String userName, @RequestParam(required = false) String phoneNumber,
                                             @RequestParam(required = false) String birthday,
                                             @RequestParam(required = false) String address) {
        try {
            MemberService.WalkInResult result = memberService.registerWalkIn(userName, phoneNumber, birthday, address);
            return ResponseEntity.ok(Map.of("userId", result.userId(), "loginId", result.loginId(),
                    "tempPassword", result.tempPassword()));
        } catch (MemberException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /** admin "총괄 현황"(회원수현황) + "총괄 누계 현황"(연령별 기부건수) 리포트용 - 가입일/생년월일
     *  스냅샷. UserDetail이 없는 회원은 birthday=null로 내려간다(연령대 집계에서 "미상"으로 처리). */
    @GetMapping("/api/admin/members/all")
    public List<MemberSnapshotDto> allForResync() {
        Map<Long, UserDetail> detailByUserId = userDetailRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(UserDetail::getUserId, d -> d, (a, b) -> a));
        return userRepository.findAll().stream()
                .map(u -> new MemberSnapshotDto(u.getUserId(), parseDate(u.getCreatedDate()),
                        detailByUserId.containsKey(u.getUserId()) ? detailByUserId.get(u.getUserId()).getBirthday() : null))
                .toList();
    }

    public record MemberSnapshotDto(Long userId, LocalDateTime createdDate, String birthday) {
    }

    /** admin "회원 로그인 로그 관리" 화면용 (AS-IS opmanager/log/user-login-log 재현) - member가
     *  이미 SFR-002로 쓰고 있는 실제 로그인 감사 이력을 그대로 노출한다. */
    @GetMapping("/api/admin/login-log")
    public List<LoginLogDto> loginLog() {
        return loginLogRepository.findTop200ByOrderByLoginLogIdDesc().stream()
                .map(l -> new LoginLogDto(l.getLoginLogId(), l.getLoginType(), l.getLoginId(),
                        l.getSuccessFlag(), l.getRemoteAddr(), l.getMemo(), l.getLoginDate()))
                .toList();
    }

    public record LoginLogDto(Integer loginLogId, String loginType, String loginId, String successFlag,
                               String remoteAddr, String memo, String loginDate) {
    }

    /** User/LoginLog의 날짜 필드는 AS-IS 레거시 컨벤션(yyyyMMddHHmmss 문자열)이라 cross-service
     *  DTO 계약(LocalDateTime, admin report-statistics-suite가 이미 소비 중)을 위해 파싱한다. */
    private static LocalDateTime parseDate(String value) {
        return value == null || value.isBlank() ? null : LocalDateTime.parse(value, DATE_FORMAT);
    }

    /** admin "회원 액션 로그 관리" 화면용 (AS-IS opmanager/log/user-action-log 재현). */
    @GetMapping("/api/admin/user-action-log")
    public List<UserActionLogDto> userActionLog() {
        return userActionLogRepository.findTop200ByOrderByActionLogIdDesc().stream()
                .map(l -> new UserActionLogDto(l.getActionLogId(), l.getCreatedDate(), l.getRemoteAddr(),
                        l.getRequestUri(), l.getRequestMethod(), l.getLoginId()))
                .toList();
    }

    public record UserActionLogDto(Integer actionLogId, String createdDate, String remoteAddr,
                                    String requestUri, String requestMethod, String loginId) {
    }

    /** admin "회원 권한/상태변경 이력 관리" 화면용 (SFR-002 "권한 변경, 계정 잠금/해제" 감사로그 -
     *  로그는 이미 OP_USER_CHANGE_LOG에 쌓이고 있었으나 조회할 admin 화면이 없던 gap을 닫는다). */
    @GetMapping("/api/admin/change-log")
    public List<ChangeLogDto> changeLog() {
        List<UserChangeLog> logs = userChangeLogRepository.findTop200ByOrderByChangeLogIdDesc();
        Map<Long, String> loginIdByUserId = userRepository.findAllById(
                        logs.stream().map(UserChangeLog::getUserId).distinct().toList()).stream()
                .collect(java.util.stream.Collectors.toMap(User::getUserId, User::getLoginId));
        return logs.stream()
                .map(l -> new ChangeLogDto(l.getChangeLogId(), l.getUserId(), loginIdByUserId.get(l.getUserId()),
                        l.getParameter(), l.getRemoteAddr(), l.getCreatedDate()))
                .toList();
    }

    public record ChangeLogDto(Long changeLogId, Long userId, String loginId, String parameter,
                                String remoteAddr, String createdDate) {
    }
}
