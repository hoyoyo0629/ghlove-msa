package com.ghlove.member.service;

import com.ghlove.member.domain.User;
import com.ghlove.member.domain.UserChangeLog;
import com.ghlove.member.domain.UserDetail;
import com.ghlove.member.domain.UserRole;
import com.ghlove.member.repository.UserChangeLogRepository;
import com.ghlove.member.repository.UserDetailRepository;
import com.ghlove.member.repository.UserRepository;
import com.ghlove.member.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * admin 콘솔 회원관리 콘솔 (AS-IS opmanager/user/* - UserManagerController/
 * GeneralCustomerManagerController/SecedeUserManagerController/SleepUserManagerController,
 * docs/as-is-admin-gap-deep-audit-part2.md 배치D 참고). admin 서비스는 회원 데이터를 직접
 * 갖고 있지 않아(MSA 서비스 경계) admin/service/MemberAdminClient가 여기 REST API를 호출한다.
 *
 * 검색 화면들은 전부 저트래픽(관리자 콘솔)이라 OffgiveController와 동일한 관행으로 날짜범위만
 * DB 쿼리로 좁히고 나머지 필터(아이디/이름/주소 등)는 인메모리로 처리한다.
 */
@Service
@RequiredArgsConstructor
public class AdminMemberService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String STATUS_WITHDRAWN = "WITHDRAWN";
    private static final String STATUS_DORMANT = "DORMANT";
    private static final String STATUS_ACTIVE = "ACTIVE";

    private final UserRepository userRepository;
    private final UserDetailRepository userDetailRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserChangeLogRepository userChangeLogRepository;

    /** D3 일반회원 검색 (AS-IS GeneralCustomerManagerController) - 가입일 범위 기본값 오늘. */
    public MemberSearchResultDto search(String fromDate, String toDate, String srchKey, String srchValue,
                                         String sbscrbSeCode, String receiveEmail, int page, int size) {
        List<User> users = userRepository.findByCreatedDateBetweenOrderByCreatedDateDesc(
                rangeStart(fromDate), rangeEnd(toDate));

        if (sbscrbSeCode != null && !sbscrbSeCode.isBlank()) {
            users = users.stream().filter(u -> sbscrbSeCode.equals(u.getSbscrbSeCode())).toList();
        }

        Map<Long, UserDetail> detailByUserId = detailsFor(users);

        if (receiveEmail != null && !receiveEmail.isBlank()) {
            users = users.stream().filter(u -> {
                UserDetail d = detailByUserId.get(u.getUserId());
                return d != null && receiveEmail.equals(d.getReceiveEmail());
            }).toList();
        }

        if (srchKey != null && srchValue != null && !srchValue.isBlank()) {
            String v = srchValue.trim();
            users = users.stream().filter(u -> switch (srchKey) {
                case "USER_NAME" -> u.getUserName() != null && u.getUserName().contains(v);
                case "ADDRESS" -> {
                    UserDetail d = detailByUserId.get(u.getUserId());
                    yield d != null && d.getAddress() != null && d.getAddress().contains(v);
                }
                default -> u.getLoginId() != null && u.getLoginId().contains(v); // LOGIN_ID
            }).toList();
        }

        Page<User> paged = paginate(users, page, size);
        List<MemberSearchRowDto> content = paged.items().stream()
                .map(u -> {
                    UserDetail d = detailByUserId.get(u.getUserId());
                    return new MemberSearchRowDto(u.getUserId(), u.getLoginId(), u.getUserName(), u.getEmail(),
                            u.getStatusCode(), u.getSbscrbSeCode(), u.getCreatedDate(),
                            d != null ? d.getPhoneNumber() : null);
                }).toList();
        return new MemberSearchResultDto(content, paged.totalElements(), paged.totalPages());
    }

    public MemberDetailDto detail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MemberException("회원 정보를 찾을 수 없습니다."));
        UserDetail detail = userDetailRepository.findById(userId).orElseGet(UserDetail::new);
        List<String> roles = userRoleRepository.findByUserId(userId).stream()
                .map(UserRole::getAuthority).toList();
        return new MemberDetailDto(user.getUserId(), user.getLoginId(), user.getUserName(), user.getEmail(),
                user.getStatusCode(), user.getSbscrbSeCode(), user.getCreatedDate(), user.getUpdatedDate(),
                user.getLoginDate(), user.getLoginCount(), user.getLeaveDate(),
                detail.getPhoneNumber(), detail.getAddress(), detail.getAddressDetail(), detail.getPost(),
                detail.getGender(), detail.getBirthday(), detail.getReceiveEmail(), detail.getReceiveSms(),
                detail.getReceiveKakao(), detail.getLeaveReason(), detail.getLeaveCode(), roles);
    }

    /** 관리자에 의한 강제 탈퇴처리 - 본인이 비밀번호를 입력하는 일반 탈퇴(withdraw())와 달리
     *  비밀번호 확인 없이 운영자가 즉시 처리한다(AS-IS opmanager 회원상태변경 재현). */
    @Transactional
    public void adminWithdraw(Long userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MemberException("회원 정보를 찾을 수 없습니다."));
        if (STATUS_WITHDRAWN.equals(user.getStatusCode())) {
            throw new MemberException("이미 탈퇴한 회원입니다.");
        }
        user.setStatusCode(STATUS_WITHDRAWN);
        user.setLeaveDate(now());
        user.setUpdatedDate(now());
        userRepository.save(user);

        UserDetail detail = userDetailRepository.findById(userId).orElseGet(() -> {
            UserDetail d = new UserDetail();
            d.setUserId(userId);
            d.setLevelId(1);
            return d;
        });
        detail.setLeaveReason(reason);
        userDetailRepository.save(detail);

        recordChangeLog(userId, "ADMIN_WITHDRAW" + (reason == null || reason.isBlank() ? "" : ": " + reason));
    }

    /** D4 탈퇴회원 조회 (AS-IS SecedeUserManagerController) - 탈퇴일 범위 기본값 오늘. */
    public SecedeSearchResultDto searchSecede(String fromDate, String toDate, String srchKey, String srchValue,
                                               int page, int size) {
        List<User> users = userRepository.findByStatusCodeAndLeaveDateBetweenOrderByLeaveDateDesc(
                STATUS_WITHDRAWN, rangeStart(fromDate), rangeEnd(toDate));
        Map<Long, UserDetail> detailByUserId = detailsFor(users);

        if (srchKey != null && srchValue != null && !srchValue.isBlank()) {
            String v = srchValue.trim();
            users = users.stream().filter(u -> {
                if ("LEAVE_REASON".equals(srchKey)) {
                    UserDetail d = detailByUserId.get(u.getUserId());
                    return d != null && d.getLeaveReason() != null && d.getLeaveReason().contains(v);
                }
                return u.getLoginId() != null && u.getLoginId().contains(v); // LOGIN_ID
            }).toList();
        }

        Page<User> paged = paginate(users, page, size);
        List<SecedeRowDto> content = paged.items().stream()
                .map(u -> {
                    UserDetail d = detailByUserId.get(u.getUserId());
                    return new SecedeRowDto(u.getUserId(), u.getLoginId(), u.getUserName(), u.getLeaveDate(),
                            d != null ? d.getLeaveReason() : null, d != null ? d.getLeaveCode() : null);
                }).toList();
        return new SecedeSearchResultDto(content, paged.totalElements(), paged.totalPages());
    }

    /** D5 휴면회원 조회/해제 (AS-IS SleepUserManagerController) - 최종 로그인일 범위 기본값 오늘
     *  (AS-IS 화면 그대로 - 휴면회원은 정의상 로그인이 오래됐으므로 오늘 범위로는 보통 비어있고,
     *  운영자가 직접 기간을 넓혀 조회하는 UX다). */
    public SleepSearchResultDto searchSleep(String fromDate, String toDate, String srchKey, String srchValue,
                                             int page, int size) {
        List<User> users = userRepository.findByStatusCodeAndLoginDateBetweenOrderByLoginDateDesc(
                STATUS_DORMANT, rangeStart(fromDate), rangeEnd(toDate));

        if (srchKey != null && srchValue != null && !srchValue.isBlank()) {
            String v = srchValue.trim();
            users = users.stream().filter(u -> "USER_NAME".equals(srchKey)
                    ? (u.getUserName() != null && u.getUserName().contains(v))
                    : (u.getLoginId() != null && u.getLoginId().contains(v))).toList();
        }

        Page<User> paged = paginate(users, page, size);
        List<SleepRowDto> content = paged.items().stream()
                .map(u -> new SleepRowDto(u.getUserId(), u.getLoginId(), u.getUserName(), u.getLoginDate(), u.getCreatedDate()))
                .toList();
        return new SleepSearchResultDto(content, paged.totalElements(), paged.totalPages());
    }

    /** 휴면 해제(다중선택) - DormancyController.reactivate()와 달리 본인 비밀번호 확인 없이
     *  운영자가 즉시 여러 건을 일괄 해제한다. */
    @Transactional
    public int wakeup(List<Long> userIds) {
        int count = 0;
        for (Long id : userIds) {
            User user = userRepository.findById(id).orElse(null);
            if (user != null && STATUS_DORMANT.equals(user.getStatusCode())) {
                user.setStatusCode(STATUS_ACTIVE);
                user.setUpdatedDate(now());
                userRepository.save(user);
                recordChangeLog(id, "DORMANT_RELEASED_BY_ADMIN");
                count++;
            }
        }
        return count;
    }

    private Map<Long, UserDetail> detailsFor(List<User> users) {
        List<Long> ids = users.stream().map(User::getUserId).toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return userDetailRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(UserDetail::getUserId, d -> d, (a, b) -> a));
    }

    private static Page<User> paginate(List<User> all, int page, int size) {
        int totalElements = all.size();
        int totalPages = (int) Math.ceil(totalElements / (double) Math.max(size, 1));
        int from = Math.min(Math.max(page, 0) * size, totalElements);
        int to = Math.min(from + size, totalElements);
        return new Page<>(all.subList(from, to), totalElements, totalPages);
    }

    private record Page<T>(List<T> items, long totalElements, int totalPages) {
    }

    private static String rangeStart(String dateStr) {
        return parseOrToday(dateStr).format(DateTimeFormatter.BASIC_ISO_DATE) + "000000";
    }

    private static String rangeEnd(String dateStr) {
        return parseOrToday(dateStr).format(DateTimeFormatter.BASIC_ISO_DATE) + "235959";
    }

    private static LocalDate parseOrToday(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return LocalDate.now();
        }
        return LocalDate.parse(dateStr);
    }

    private void recordChangeLog(Long userId, String parameter) {
        UserChangeLog log = new UserChangeLog();
        log.setUserId(userId);
        log.setParameter(parameter);
        log.setManagerId(userId);
        log.setCreatedDate(now());
        userChangeLogRepository.save(log);
    }

    private static String now() {
        return DATE_FORMAT.format(LocalDateTime.now());
    }

    public record MemberSearchRowDto(Long userId, String loginId, String userName, String email, String statusCode,
                                      String sbscrbSeCode, String createdDate, String phoneNumber) {
    }

    public record MemberSearchResultDto(List<MemberSearchRowDto> content, long totalElements, int totalPages) {
    }

    public record MemberDetailDto(Long userId, String loginId, String userName, String email, String statusCode,
                                   String sbscrbSeCode, String createdDate, String updatedDate, String loginDate,
                                   Integer loginCount, String leaveDate, String phoneNumber, String address,
                                   String addressDetail, String post, String gender, String birthday,
                                   String receiveEmail, String receiveSms, String receiveKakao, String leaveReason,
                                   String leaveCode, List<String> roles) {
    }

    public record SecedeRowDto(Long userId, String loginId, String userName, String leaveDate, String leaveReason,
                                String leaveCode) {
    }

    public record SecedeSearchResultDto(List<SecedeRowDto> content, long totalElements, int totalPages) {
    }

    public record SleepRowDto(Long userId, String loginId, String userName, String loginDate, String createdDate) {
    }

    public record SleepSearchResultDto(List<SleepRowDto> content, long totalElements, int totalPages) {
    }
}
