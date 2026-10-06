package com.ghlove.member.service;

import com.ghlove.member.domain.IndvdlinfoReadngHist;
import com.ghlove.member.domain.User;
import com.ghlove.member.domain.UserChangeLog;
import com.ghlove.member.domain.UserDetail;
import com.ghlove.member.domain.UserRole;
import com.ghlove.member.domain.UserRoleId;
import com.ghlove.member.repository.IndvdlinfoReadngHistRepository;
import com.ghlove.member.repository.UserChangeLogRepository;
import com.ghlove.member.repository.UserDeliveryRepository;
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
    private static final String STATUS_LOCKED = "LOCKED";
    private static final String ROLE_USER = "ROLE_USER";

    private final UserRepository userRepository;
    private final UserDetailRepository userDetailRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserChangeLogRepository userChangeLogRepository;
    private final UserDeliveryRepository userDeliveryRepository;
    private final IndvdlinfoReadngHistRepository indvdlinfoReadngHistRepository;
    private final MemberService memberService;
    private final PointClient pointClient;

    /**
     * D3 일반회원 검색 (AS-IS GeneralCustomerManagerController) - 가입일 범위 기본값 오늘.
     *
     * <p>AS-IS 쿼리는 {@code WHERE U.STATUS_CODE = 9}로 <b>정상회원만</b> 조회한다
     * (slave-generalcustomer-mapper.getGeneralCustomerListByParam) - 탈퇴·휴면·차단 회원은
     * 이 화면에 나오지 않고 각자의 전용 화면(4105 탈퇴회원리스트 / 4107 휴면회원관리)에서 본다.
     * 그 조건이 빠져 있어 세 화면의 모집단이 겹쳐 있었다.
     */
    public MemberSearchResultDto search(String fromDate, String toDate, String srchKey, String srchValue,
                                         String sbscrbSeCode, String receiveEmail, int page, int size) {
        List<User> users = userRepository.findByCreatedDateBetweenOrderByCreatedDateDesc(
                        rangeStart(fromDate), rangeEnd(toDate)).stream()
                .filter(u -> STATUS_ACTIVE.equals(u.getStatusCode()))
                .toList();

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
                            d != null ? d.getPhoneNumber() : null,
                            d != null ? d.getAddress() : null, d != null ? d.getAddressDetail() : null,
                            d != null ? d.getReceiveEmail() : null);
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
                detail.getReceiveKakao(), detail.getLeaveReason(), detail.getLeaveCode(), roles,
                user.getUserKey() != null && !user.getUserKey().isBlank());
    }

    /**
     * 회원 이름 일괄 조회 - admin 화면이 회원ID만 가진 목록에 이름을 채울 때 쓴다
     * (기부혜택증 열람현황 메뉴 19103이 첫 사용처: AS-IS는 OP_USER를 INNER JOIN해 사용자명을
     * 찍는데 TO-BE에서 회원은 member 소유다).
     *
     * <p><b>AS-IS가 INNER JOIN</b>이므로 회원을 찾지 못한 행은 호출부가 버려야 한다 - 그래서
     * 없는 ID는 결과에 넣지 않는다(존재 여부 판정에도 쓸 수 있게).
     */
    public Map<Long, String> userNames(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        return userRepository.findAllById(userIds).stream()
                .filter(u -> u.getUserName() != null)
                .collect(Collectors.toMap(User::getUserId, User::getUserName, (a, b) -> a));
    }

    /**
     * 회원ID → 로그인ID 일괄조회. 첫 사용처는 admin <b>Q&A 관리(메뉴 5112)</b> 목록이다 -
     * AS-IS는 {@code OP_QNA}에 {@code OP_USER}를 조인해 {@code LOGIN_ID}를 같이 뽑지만,
     * TO-BE에서 회원은 member 소유라 조인할 수 없어 코드만 받아 여기서 채운다
     * ({@link #userNames(List)}와 같은 패턴).
     *
     * <p>AS-IS 조인 조건이 {@code U.STATUS_CODE = '9'}(정상회원)이므로 같은 조건을 적용한다(TO-BE 상태값은 ACTIVE - 담당자 9/2 번역과 같은 관례) -
     * 탈퇴·휴면 회원이면 AS-IS도 로그인ID가 비어 보인다.
     */
    public Map<Long, String> userLoginIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        return userRepository.findAllById(userIds).stream()
                .filter(u -> u.getLoginId() != null && STATUS_ACTIVE.equals(u.getStatusCode()))
                .collect(Collectors.toMap(User::getUserId, User::getLoginId, (a, b) -> a));
    }

    /**
     * 로그인ID 부분일치로 회원ID를 찾는다 - admin Q&A 관리의 <b>검색구분 '아이디'</b>용이다.
     * AS-IS는 {@code U.LOGIN_ID LIKE '%검색어%'}를 목록 SQL 안에서 걸지만, TO-BE는 회원이
     * 다른 서비스라 <b>먼저 회원ID를 받아와 그걸로 걸러야</b> 한다(기부 합계 조회와 같은 방식).
     */
    public List<Long> userIdsByLoginIdLike(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        return userRepository.findByLoginIdContainingAndStatusCode(keyword.trim(), STATUS_ACTIVE)
                .stream().map(User::getUserId).toList();
    }

    /**
     * 국민비서(IPS) 문자 발송에 필요한 수신자 정보 - AS-IS {@code QnaMapper.getQnaUserInfo}
     * ({@code GiveUserSmsInfo})를 서비스 경계 밖으로 옮긴 것이다.
     *
     * <p>AS-IS SQL은 {@code OP_QNA → OP_USER → OP_USER_DETAIL}을 <b>INNER JOIN</b>하고
     * {@code USER_NAME, RECEIVE_SMS, MBER_CI, REPLACE(PHONE_NUMBER,'-','')}를 뽑는다.
     * 문의 행은 admin이 가지고 있으니 여기서는 <b>회원ID로 두 표만</b> 조인한다 -
     * INNER JOIN이라 회원이 없거나 상세정보가 없으면 AS-IS도 결과가 없으므로 {@code null}을 돌려준다.
     *
     * <p><b>CI와 전화번호가 나간다</b> - 국민비서 연계가 CI를 수신자 식별값으로 요구해서다
     * (AS-IS {@code PRVC_IDNTFC_INFO = MBER_CI}). 호출은 admin 전용 비밀키 게이트 안이다.
     */
    public SmsReceiverDto smsReceiver(Long userId) {
        if (userId == null || userId <= 0) {
            return null;
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }
        UserDetail detail = userDetailRepository.findById(userId).orElse(null);
        if (detail == null) {
            return null;
        }
        String phoneNumber = detail.getPhoneNumber() == null ? null : detail.getPhoneNumber().replace("-", "");
        return new SmsReceiverDto(userId, user.getUserName(), phoneNumber,
                detail.getReceiveSms(), user.getMberCi());
    }

    /** AS-IS GiveUserSmsInfo에서 Q&A 답변 문자가 쓰는 네 값만 옮긴 것. */
    public record SmsReceiverDto(Long userId, String userName, String phoneNumber,
                                 String receiveSms, String mberCi) {
    }

    /**
     * 일반회원관리 상세 > 배송지 관리 팝업 (AS-IS GeneralCustomerManagerController
     * {@code GET /delivery/{userId}} → generalCustomerService.userDeliveryList).
     *
     * <p>AS-IS는 마스킹을 적용하려고 전용 매퍼 대신 {@code userDeliveryService.getUserDeliveryList}를
     * 쓴다(주석에 그렇게 적혀 있다). 이 프로젝트는 운영자 콘솔에서 개인정보를 마스킹하지 않는
     * 관행이라([[admin-pii-display-no-masking]]) 그대로 내려준다.
     */
    public List<UserDeliveryRowDto> deliveries(Long userId) {
        return userDeliveryRepository.findByUserIdOrderByDefaultFlagDescCreatedDateDesc(userId).stream()
                .map(d -> new UserDeliveryRowDto(d.getUserDeliveryId(), d.getTitle(), d.getDefaultFlag(),
                        d.getUserName(), d.getAddress(), d.getAddressDetail(), d.getMobile(), d.getPhone()))
                .toList();
    }

    /**
     * 개인정보 열람 이력 저장 (AS-IS {@code getGeneralCustomerDetailsNoMasking}이 상세를
     * 돌려주기 전에 {@code G_INDVDLINFO_READNG_HIST}에 남기는 그 이력).
     *
     * @param managerUserId 열람한 운영자의 USER_ID, {@code targetUserId} 열람 대상 회원
     */
    @Transactional
    public void recordPiiAccess(Long managerUserId, Long targetUserId) {
        IndvdlinfoReadngHist hist = new IndvdlinfoReadngHist();
        hist.setUserId(managerUserId);
        hist.setTrgetUserId(targetUserId);
        hist.setReadngDt(LocalDateTime.now());
        indvdlinfoReadngHistRepository.save(hist);
    }

    /** 관리자에 의한 강제 탈퇴처리 - 본인이 비밀번호를 입력하는 일반 탈퇴(withdraw())와 달리
     *  비밀번호 확인 없이 운영자가 즉시 처리한다(AS-IS opmanager 회원상태변경 재현). */
    @Transactional
    public void adminWithdraw(Long userId, String reason) {
        adminWithdraw(userId, reason, null);
    }

    /**
     * 일반회원관리(메뉴 4101) 상세의 회원탈퇴 - AS-IS
     * {@code updateGeneralCustomerSecedeProcess}의 결과코드 분기를 그대로 따른다.
     *
     * <p>AS-IS는 상태코드가 이미 탈퇴면 {@code ERR_ALR_SECEDE}, <b>디지털원패스 회원이면
     * {@code ERR_ONE_PASS}</b>로 거절한다(원패스는 그쪽에서 연동해지를 해야 한다).
     *
     * @param leaveUserId 탈퇴를 처리한 운영자의 USER_ID. AS-IS가
     *        {@code OP_USER_DETAIL.LEAVE_USER_ID}에 남기는 값이고, 탈퇴회원리스트(메뉴 4105)의
     *        <b>탈퇴구분(관리자탈퇴/회원탈퇴)·담당자 컬럼이 이 값으로 갈린다</b> - 안 남기면
     *        관리자가 처리한 탈퇴가 회원탈퇴로 보인다.
     */
    @Transactional
    public void adminWithdraw(Long userId, String reason, Long leaveUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MemberException("회원 정보를 찾을 수 없습니다."));
        if (STATUS_WITHDRAWN.equals(user.getStatusCode())) {
            throw new MemberException("이미 탈퇴한 회원입니다.");
        }
        if (user.getUserKey() != null && !user.getUserKey().isBlank()) {
            throw new OnePassMemberException("디지털원패스 회원은 탈퇴 처리 불가능합니다.");
        }
        // 본인 탈퇴와 마찬가지로 그 해 기부액을 CI 기준으로 남겨야 재가입 시 연간 한도가
        // 이어진다 - 강제 탈퇴라고 해서 한도가 초기화되면 안 된다.
        memberService.snapshotDonationForLimitCarryOver(user);
        // 잔여 기부포인트도 본인 탈퇴와 동일하게 소멸시킨다(AS-IS GeneralCustomerServiceImpl:286~296).
        pointClient.expireAllOnWithdrawal(userId);

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
        if (leaveUserId != null) {
            detail.setLeaveUserId(leaveUserId);
        }
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
                            d != null ? d.getLeaveReason() : null, d != null ? d.getLeaveCode() : null,
                            d != null ? d.getLeaveUserId() : null);
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
        // 주소는 admin 휴면회원관리(메뉴 4107) 화면의 컬럼이라 같이 내려준다
        Map<Long, UserDetail> detailByUserId = detailsFor(paged.items());
        List<SleepRowDto> content = paged.items().stream()
                .map(u -> {
                    UserDetail d = detailByUserId.get(u.getUserId());
                    return new SleepRowDto(u.getUserId(), u.getLoginId(), u.getUserName(), u.getLoginDate(),
                            u.getCreatedDate(), d != null ? d.getAddress() : null,
                            d != null ? d.getAddressDetail() : null);
                })
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

    /** 계정잠금 관리자 해제 (SFR-002 "계정 잠금... 해제") - 본인 경유(find-idpw)와 달리 비밀번호
     *  확인 없이 운영자가 즉시 해제한다. */
    @Transactional
    public void unlock(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MemberException("회원 정보를 찾을 수 없습니다."));
        if (!STATUS_LOCKED.equals(user.getStatusCode())) {
            throw new MemberException("잠금 상태인 계정만 해제할 수 있습니다.");
        }
        user.setStatusCode(STATUS_ACTIVE);
        user.setLoginFailCount(0);
        user.setUpdatedDate(now());
        userRepository.save(user);
        recordChangeLog(userId, "ACCOUNT_UNLOCKED_BY_ADMIN");
    }

    /** RBAC 권한 회수 (SFR-002 "권한 부여·회수 절차 표준화") - ROLE_LOCALGOV/ROLE_PROVIDER처럼
     *  승인을 거쳐 부여된 권한을 운영자가 다시 거둬들인다. */
    @Transactional
    public void revokeRole(Long userId, String authority) {
        if (ROLE_USER.equals(authority)) {
            throw new MemberException("기본 회원 권한(ROLE_USER)은 회수할 수 없습니다.");
        }
        UserRoleId id = new UserRoleId(userId, authority);
        if (!userRoleRepository.existsById(id)) {
            throw new MemberException("보유하지 않은 권한입니다.");
        }
        userRoleRepository.deleteById(id);
        recordChangeLog(userId, "ROLE_REVOKED: " + authority);
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

    // 날짜가 비어 있으면 "오늘"이 아니라 전 구간을 조회한다(A-1). 예전엔 기본값이 당일이라
    // 운영자가 기간을 넓히기 전에는 회원/휴면/탈퇴 목록이 사실상 비어 보였다.
    private static String rangeStart(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return "00000000000000";
        }
        return LocalDate.parse(dateStr).format(DateTimeFormatter.BASIC_ISO_DATE) + "000000";
    }

    private static String rangeEnd(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return "99999999999999";
        }
        return LocalDate.parse(dateStr).format(DateTimeFormatter.BASIC_ISO_DATE) + "235959";
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

    /** {@code address}/{@code addressDetail}/{@code receiveEmail}는 일반회원관리(메뉴 4101) 목록 컬럼용. */
    public record MemberSearchRowDto(Long userId, String loginId, String userName, String email, String statusCode,
                                      String sbscrbSeCode, String createdDate, String phoneNumber,
                                      String address, String addressDetail, String receiveEmail) {
    }

    public record MemberSearchResultDto(List<MemberSearchRowDto> content, long totalElements, int totalPages) {
    }

    /** {@code onePassUser}가 true면 디지털원패스 연계회원이라 관리자가 탈퇴시킬 수 없다
     *  (AS-IS {@code ERR_ONE_PASS}). 원패스 식별키 자체는 내려주지 않는다 - 판정만 필요하다. */
    public record MemberDetailDto(Long userId, String loginId, String userName, String email, String statusCode,
                                   String sbscrbSeCode, String createdDate, String updatedDate, String loginDate,
                                   Integer loginCount, String leaveDate, String phoneNumber, String address,
                                   String addressDetail, String post, String gender, String birthday,
                                   String receiveEmail, String receiveSms, String receiveKakao, String leaveReason,
                                   String leaveCode, List<String> roles, boolean onePassUser) {
    }

    /** 일반회원관리 상세 > 배송지 관리 팝업의 한 행. */
    public record UserDeliveryRowDto(Long userDeliveryId, String title, String defaultFlag, String userName,
                                      String address, String addressDetail, String mobile, String phone) {
    }

    /** {@code leaveUserId}는 AS-IS 탈퇴구분 판정에 쓴다 - 값이 있으면 관리자탈퇴, 없으면 회원탈퇴
     *  (AS-IS secedeuser-mapper: {@code IFNULL(UD.LEAVE_USER_ID,'') != ''}). 담당자 이름·권한그룹은
     *  admin이 자기 OP_MANAGER/OP_ROLE에서 이 ID로 찾는다. */
    public record SecedeRowDto(Long userId, String loginId, String userName, String leaveDate, String leaveReason,
                                String leaveCode, Long leaveUserId) {
    }

    public record SecedeSearchResultDto(List<SecedeRowDto> content, long totalElements, int totalPages) {
    }

    /** {@code address}/{@code addressDetail}는 admin 휴면회원관리(메뉴 4107) 화면의 "주소" 컬럼용이다. */
    public record SleepRowDto(Long userId, String loginId, String userName, String loginDate, String createdDate,
                               String address, String addressDetail) {
    }

    public record SleepSearchResultDto(List<SleepRowDto> content, long totalElements, int totalPages) {
    }
}
