package com.ghlove.admin.service;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.repository.ManagerRepository;
import com.ghlove.admin.web.support.OffPersonInChargeSearchParam;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 오프라인담당자 (메뉴 4601) - AS-IS saleson.shop.user.OffPersonInChargeManagerController
 * ({@code /opmanager/user/off-charger}) + PersonInChargeServiceImpl의 오프라인 전용 메서드
 * ({@code insertOffPersonInCharge}/{@code updateOffPersonInCharge}/{@code getOffPersonInChargeMainCount}/
 * {@code updateStatusCode}/{@code updatePasswordInit}) 재현.
 *
 * <p>AS-IS에서 이 화면이 다루는 것은 농협 창구에서 오프라인 기부를 접수하는 <b>지점 담당자</b>다
 * (ROLE_ADMIN_7 오프라인 주담당자 / ROLE_ADMIN_8 오프라인 부담당자). 지자체담당자(4402)와 달리
 * 소속이 <b>지자체가 아니라 지점(BANK_CODE + PSITN_NM)</b>이고, 승인요청 큐를 거치지 않고
 * 관리자가 이 화면에서 바로 계정을 만든다.
 *
 * <p><b>주담당자 정원은 지점(BANK_CODE) 단위다</b> - AS-IS
 * {@code personincharge-mapper.getOffPersonInChargeMainCount}가
 * {@code BANK_CODE = #{bankCode} AND STATUS_CODE = 9 AND USER_ID != #{userId}}로 센다
 * (사용중인 사람만, 본인 제외). 지자체담당자의 정원검사가 LOCGOV_CODE 단위인 것과 대응된다.
 *
 * <p><b>AS-IS 결함 1건 - 고쳤다</b>: 등록(insertOffPersonInCharge)이
 * {@code user.setStatusCode(String.valueOf(personInCharge.getStatusCode()))}로 상태코드를 넣는데,
 * 등록 폼(form.jsp)에는 사용여부 입력칸이 없어 {@code statusCode}(Long)가 항상 null이다 →
 * {@code String.valueOf((Object) null)}이 문자열 {@code "null"}이 되어 STATUS_CODE='null'로 INSERT된다.
 * 목록·상세 조회가 모두 {@code STATUS_CODE IN (9, 2)}로 걸러지고 로그인 쿼리도 {@code = '9'}를
 * 요구하므로, 방금 등록한 담당자가 목록에 보이지도 로그인하지도 못한다(등록 기능이 사실상 불구).
 * TO-BE는 등록 시 '사용'({@code ACTIVE})로 넣는다.
 *
 * <p>AS-IS와 다른 점(TO-BE 구조상 불가피):
 * <ul>
 *   <li>AS-IS 등록은 OP_USER·OP_USER_DETAIL·OP_USER_ROLE·OP_MANAGER 4개 테이블에 넣는다. TO-BE는
 *       회원(OP_USER)을 member 서비스가 소유하고 admin은 OP_MANAGER만 쓴다 - 이미 있는
 *       {@link ManagerAdminService}의 계정등록도 같은 방식이다. 권한은 OP_USER_ROLE이 아니라
 *       {@code OP_MANAGER.AUTHORITY}에 들어간다.</li>
 *   <li>AS-IS 아이디 중복확인은 {@code userService.checkDuplication}이라 OP_USER의 가입불가 아이디
 *       목록까지 본다. TO-BE는 OP_MANAGER만 확인한다.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class OffPersonInChargeAdminService {

    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static final String OFF_MAIN = "ROLE_ADMIN_7";
    public static final String OFF_SUB = "ROLE_ADMIN_8";

    /** AS-IS 화면문구: "주관리자는 최대 2명까지만 등록이 가능합니다." */
    public static final int MAIN_LIMIT = 2;

    /** 소속 지점 공통코드 - 011 농협은행 / 012 농축협 / 035 제주은행. */
    public static final String BANK_CODE_TYPE = "OFF_BANK_LIST";

    /** AS-IS insertOffPersonInCharge - 비밀번호를 안 받으면 넣는 기본값(농협아이디발급.xlsx 기준). */
    private static final String DEFAULT_PASSWORD = "nacf1234";

    /** 비밀번호 초기화를 할 수 없는 권한 - AS-IS updatePasswordInit의 유효성 검사. */
    private static final Set<String> PASSWORD_INIT_DENIED =
            Set.of("ROLE_ADMIN_5", "ROLE_ADMIN_6", OFF_SUB);

    private final ManagerRepository managerRepository;
    private final CommonCodeService commonCodeService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 목록 한 행 - AS-IS getChargerListByParam이 내려주는 컬럼 중 list.jsp가 쓰는 것만 담았다.
     * 지점명(BANK_NM)은 AS-IS도 OP_COMMON_CODE('OFF_BANK_LIST')에서 스칼라 서브쿼리로 붙인다.
     */
    public record Row(Long userId, String authority, String bankCode, String bankNm, String psitnNm,
                      String loginId, String userName, String empId, String statusCode, String denyDate) {

        /** AS-IS 목록의 '구분' 칸 - 주담당자만 표기하고 부담당자는 '-'다. */
        public String getAuthorityText() {
            return OFF_MAIN.equals(authority) ? "주관리자" : "-";
        }

        /** AS-IS 목록의 '소속 지점' = 은행명 + 공백 + 지점명. */
        public String getBranchText() {
            return (bankNm == null ? "" : bankNm) + " " + (psitnNm == null ? "" : psitnNm);
        }

        /** 화면은 AS-IS 그대로 9/2로 판정한다 - 컬럼값은 ACTIVE/LOCKED다. */
        public String getAsIsStatusCode() {
            return "LOCKED".equals(statusCode) ? "2" : "9";
        }

        /** AS-IS는 DATE_FORMAT(DENY_DATE,'%Y-%m-%d')로 내려준다. */
        public String getDenyDateText() {
            return formatDate(denyDate);
        }
    }

    public record SearchResult(List<Row> content, int totalElements) {
    }

    /** 소속 지점 코드목록 - AS-IS는 ROLE_ADMIN_1~4(상세)·1~4,7(등록)에만 내려준다. */
    public Map<String, String> bankCodeList() {
        return commonCodeService.labelsOf(BANK_CODE_TYPE);
    }

    /** 지점 연락처 앞자리 - AS-IS는 PHONE·TEL 두 코드그룹을 한 select에 이어 붙인다. */
    public Map<String, String> phoneAndTelCodeList() {
        Map<String, String> merged = new LinkedHashMap<>(commonCodeService.labelsOf("PHONE"));
        merged.putAll(commonCodeService.labelsOf("TEL"));
        return merged;
    }

    /**
     * 목록 조회. AS-IS 조회조건:
     * <ul>
     *   <li>권한이 ROLE_ADMIN_7·8인 사람만 (arrAuthority)</li>
     *   <li>{@code STATUS_CODE IN (9, 2)} - 탈퇴/휴면 등 다른 상태는 이 화면에 없다</li>
     *   <li>오프라인담당자(7·8)가 조회하면 <b>자기 지점(BANK_CODE)만</b></li>
     *   <li>정렬은 {@code ORDER BY BANK_NM, PSITN_NM}</li>
     * </ul>
     */
    public SearchResult search(OffPersonInChargeSearchParam param, Manager viewer, int startRow, int size) {
        Map<String, String> bankNames = bankCodeList();

        List<Row> all = managerRepository.findAllByOrderByUserIdDesc().stream()
                .filter(m -> OFF_MAIN.equals(m.getAuthority()) || OFF_SUB.equals(m.getAuthority()))
                .filter(m -> isListableStatus(m.getStatusCode()))
                .filter(m -> !isOffCharger(viewer) || (viewer.getBankCode() != null
                        && viewer.getBankCode().equals(m.getBankCode())))
                .filter(m -> param.matchesKeyword(m.getPsitnNm(), m.getLoginId(), m.getUserName(), m.getEmpId()))
                .filter(m -> param.matchesStatus(m.getStatusCode()))
                .map(m -> new Row(m.getUserId(), m.getAuthority(), m.getBankCode(),
                        bankNames.get(m.getBankCode()), m.getPsitnNm(), m.getLoginId(), m.getUserName(),
                        m.getEmpId(), m.getStatusCode(), m.getDenyDate()))
                .sorted(Comparator.comparing((Row r) -> nullsLast(r.bankNm()))
                        .thenComparing(r -> nullsLast(r.psitnNm())))
                .toList();

        int from = Math.min(Math.max(startRow, 0), all.size());
        int to = Math.min(from + Math.max(size, 1), all.size());
        return new SearchResult(all.subList(from, to), all.size());
    }

    /**
     * 상세 조회 - AS-IS getChargerDetails도 {@code STATUS_CODE IN (9,2)} + 권한 7·8 조건을 걸어서,
     * 해당하지 않으면 <b>null</b>을 돌려준다(화면이 "잘못된 접근 입니다."를 띄운다).
     */
    public Manager getChargerDetails(Long userId) {
        if (userId == null) {
            return null;
        }
        return managerRepository.findById(userId)
                .filter(m -> OFF_MAIN.equals(m.getAuthority()) || OFF_SUB.equals(m.getAuthority()))
                .filter(m -> isListableStatus(m.getStatusCode()))
                .orElse(null);
    }

    /** 은행명(목록·상세의 BANK_NM). */
    public String bankName(String bankCode) {
        return bankCodeList().get(bankCode);
    }

    /**
     * 오프라인 주담당자 인원수 - AS-IS {@code getOffPersonInChargeMainCount}:
     * 같은 지점(BANK_CODE) + 사용중(STATUS_CODE=9) + 본인 제외.
     */
    public int getOffPersonInChargeMainCount(String bankCode, Long excludeUserId) {
        return (int) managerRepository.findAllByOrderByUserIdDesc().stream()
                .filter(m -> OFF_MAIN.equals(m.getAuthority()))
                .filter(m -> bankCode != null && bankCode.equals(m.getBankCode()))
                .filter(m -> ManagerAdminService.STATUS_ACTIVE.equals(m.getStatusCode()))
                .filter(m -> excludeUserId == null || !excludeUserId.equals(m.getUserId()))
                .count();
    }

    /** AS-IS 아이디 중복확인 - 이미 쓰고 있는 아이디면 true(화면은 "중복된 아이디입니다."). */
    public boolean isOccupiedLoginId(String loginId) {
        return loginId != null && !loginId.isBlank() && managerRepository.findByLoginId(loginId).isPresent();
    }

    /**
     * 등록 - AS-IS {@code insertOffPersonInCharge}.
     *
     * <p>AS-IS 그대로인 것: 비밀번호 미입력 시 {@code nacf1234}, 직책명(OFCPS_NM)을 소속지점 코드로
     * 결정(011 농협 / 012 농축협 / 그 외 제주은행), 권한은 라디오값과 무관하게 <b>항상
     * 부담당자</b>(AS-IS는 {@code roles = {ROLE_ADMIN_8, ROLE_OPMANAGER, ROLE_USER}} 고정이고
     * 등록 폼의 라디오도 둘 다 disabled다), INFO_UPDT_DE를 등록일로 넣는다.
     *
     * <p>상태코드는 AS-IS 결함을 고쳐 '사용'으로 넣는다(클래스 주석 참고).
     */
    @Transactional
    public void insertOffPersonInCharge(String loginId, String password, String userName, String phoneNumber,
                                         String bankCode, String psitnNm, String empId, String email) {
        Manager manager = new Manager();
        manager.setLoginId(loginId);
        manager.setPassword(passwordEncoder.encode(
                password == null || password.isBlank() ? DEFAULT_PASSWORD : password));
        manager.setUserName(userName);
        manager.setPhoneNumber(phoneNumber);
        manager.setBankCode(bankCode);
        manager.setPsitnNm(psitnNm);
        manager.setEmpId(empId);
        manager.setEmail(email);
        manager.setOfcpsNm(defaultOfcpsNm(bankCode));
        manager.setAuthority(OFF_SUB);
        manager.setStatusCode(ManagerAdminService.STATUS_ACTIVE);
        manager.setLoginCount(0);
        manager.setLoginFailCount(0);
        manager.setInfoUpdtDe(today());
        manager.setCreatedDate(now());
        manager.setUpdatedDate(now());
        managerRepository.save(manager);
    }

    /**
     * 수정 - AS-IS {@code updateOffPersonInCharge} + 권한 재등록(deletePersonInChargeUserRole /
     * insertPersonInChargeUserRole).
     *
     * <p>AS-IS 그대로인 것:
     * <ul>
     *   <li>직책명(OFCPS_NM)에 <b>이메일을 그대로</b> 넣는다 - AS-IS 매퍼가
     *       {@code EMAIL = #{email}, OFCPS_NM = #{email}}이다. 2025-10-29에 '직책' 입력칸을
     *       '이메일'로 바꾸면서 두 컬럼에 같이 쓰도록 한 것이라고 매퍼 주석에 적혀 있다.</li>
     *   <li>INFO_UPDT_DE는 <b>비어 있을 때만</b> 오늘로 채운다.</li>
     *   <li>중지로 바꾸면 DENY_DATE를 지금으로, 사용으로 바꾸면 NULL로.</li>
     * </ul>
     *
     * @param asIsStatusCode 화면이 보내는 AS-IS 표기('9'/'2'). null·공백이면 상태를 건드리지 않는다
     *                       (부담당자 화면에는 사용여부 입력칸이 없어 보내지 않는다).
     */
    @Transactional
    public void updateOffPersonInCharge(Long userId, String authority, String userName, String phoneNumber,
                                         String bankCode, String psitnNm, String empId, String email,
                                         String asIsStatusCode) {
        Manager manager = managerRepository.findById(userId)
                .orElseThrow(() -> new ManagerException("오프라인담당자 계정을 찾을 수 없습니다."));

        manager.setUserName(userName);
        manager.setPhoneNumber(phoneNumber);
        manager.setBankCode(bankCode);
        manager.setPsitnNm(psitnNm);
        manager.setEmpId(empId);
        manager.setEmail(email);
        manager.setOfcpsNm(email);
        if (authority != null && !authority.isBlank()) {
            manager.setAuthority(authority);
        }
        if (manager.getInfoUpdtDe() == null || manager.getInfoUpdtDe().isBlank()) {
            manager.setInfoUpdtDe(today());
        }
        applyStatus(manager, asIsStatusCode);
        manager.setUpdatedDate(now());
        managerRepository.save(manager);
    }

    /**
     * 목록의 사용/중지 일괄변경 - AS-IS {@code updateStatusCode}.
     * 중지면 DENY_DATE를 지금으로, 사용이면 NULL로 바꾼다.
     */
    @Transactional
    public int updateStatusCode(List<Long> userIdList, String asIsStatusCode) {
        if (userIdList == null || userIdList.isEmpty()) {
            return 0;
        }
        int updated = 0;
        for (Long userId : userIdList) {
            Manager manager = managerRepository.findById(userId).orElse(null);
            if (manager == null) {
                continue;
            }
            applyStatus(manager, asIsStatusCode);
            manager.setUpdatedDate(now());
            managerRepository.save(manager);
            updated++;
        }
        return updated;
    }

    /** 삭제 - AS-IS deleteCharger(계정 + 권한행 삭제. TO-BE는 권한이 같은 행에 있다). */
    @Transactional
    public void deleteCharger(List<Long> userIdList) {
        if (userIdList == null || userIdList.isEmpty()) {
            throw new ManagerException("처리할 항목을 선택해 주세요.");
        }
        for (Long userId : userIdList) {
            if (managerRepository.existsById(userId)) {
                managerRepository.deleteById(userId);
            }
        }
    }

    /**
     * 임시 비밀번호 발급 - AS-IS {@code updatePasswordInit}. 유효성 두 개를 그대로 지킨다:
     * 요청자가 지자체 주·부담당자나 오프라인 <b>부</b>담당자면 발급하지 않고(빈 문자열),
     * 대상도 오프라인담당자(7·8)여야 한다. 발급하면 로그인 실패횟수도 초기화한다.
     *
     * @return 발급된 임시 비밀번호. 발급 불가면 빈 문자열(화면이 "임시 비밀번호 발급이
     *         불가능합니다."를 띄우고 팝업을 닫는다).
     */
    @Transactional
    public String updatePasswordInit(Long userId, Manager viewer) {
        if (viewer == null || PASSWORD_INIT_DENIED.contains(viewer.getAuthority())) {
            return "";
        }
        Manager target = managerRepository.findById(userId).orElse(null);
        if (target == null
                || !(OFF_MAIN.equals(target.getAuthority()) || OFF_SUB.equals(target.getAuthority()))) {
            return "";
        }
        String tempPassword = generateTempPassword();
        target.setPassword(passwordEncoder.encode(tempPassword));
        target.setLoginFailCount(0);
        target.setUpdatedDate(now());
        managerRepository.save(target);
        return tempPassword;
    }

    /**
     * 권한 이관 - AS-IS {@code offChargerAuthSwapProcess}. 주담당자가 다른 사람을 주담당자로
     * 올릴 때, <b>자기 자신을 부담당자 + 중지</b>로 내리고 대상을 주담당자로 올린다
     * (그래서 화면이 곧바로 로그아웃시킨다).
     */
    @Transactional
    public void authSwap(Long targetUserId, Manager viewer, String userName, String phoneNumber,
                          String bankCode, String psitnNm, String empId, String email, String asIsStatusCode) {
        Manager requester = managerRepository.findById(viewer.getUserId())
                .orElseThrow(() -> new ManagerException("로그인 계정을 찾을 수 없습니다."));

        // 1. 요청자 강등: 부담당자 + 중지(DENY_DATE 기록)
        requester.setAuthority(OFF_SUB);
        applyStatus(requester, "2");
        requester.setUpdatedDate(now());
        managerRepository.save(requester);

        // 2. 대상 승격: 화면이 보낸 정보와 함께 주담당자로
        updateOffPersonInCharge(targetUserId, OFF_MAIN, userName, phoneNumber, bankCode, psitnNm,
                empId, email, asIsStatusCode);
    }

    /** AS-IS 등록: 소속지점 코드에 따른 직책명 기본값(농협아이디발급.xlsx 기준). */
    private static String defaultOfcpsNm(String bankCode) {
        if ("011".equals(bankCode)) {
            return "농협";
        }
        if ("012".equals(bankCode)) {
            return "농축협";
        }
        return "제주은행";
    }

    /** 화면이 보내는 AS-IS 표기('9'/'2')로 상태 + 중지일자를 반영한다. */
    private static void applyStatus(Manager manager, String asIsStatusCode) {
        if (asIsStatusCode == null || asIsStatusCode.isBlank()) {
            return;
        }
        if ("2".equals(asIsStatusCode)) {
            manager.setStatusCode(ManagerAdminService.STATUS_LOCKED);
            manager.setDenyDate(now());
        } else {
            manager.setStatusCode(ManagerAdminService.STATUS_ACTIVE);
            manager.setDenyDate(null);
            manager.setLoginFailCount(0);
        }
    }

    /** AS-IS {@code STATUS_CODE IN (9, 2)} - 이 프로젝트 표기로는 ACTIVE/LOCKED다. */
    private static boolean isListableStatus(String statusCode) {
        return ManagerAdminService.STATUS_ACTIVE.equals(statusCode)
                || ManagerAdminService.STATUS_LOCKED.equals(statusCode);
    }

    private static boolean isOffCharger(Manager viewer) {
        return viewer != null
                && (OFF_MAIN.equals(viewer.getAuthority()) || OFF_SUB.equals(viewer.getAuthority()));
    }

    private static String nullsLast(String value) {
        return value == null ? "￿" : value;
    }

    /** yyyyMMddHHmmss(또는 yyyyMMdd) 문자열을 AS-IS 표기 yyyy-MM-dd로. */
    private static String formatDate(String raw) {
        if (raw == null || raw.length() < 8) {
            return "";
        }
        return raw.substring(0, 4) + "-" + raw.substring(4, 6) + "-" + raw.substring(6, 8);
    }

    /** AS-IS RandomStringUtils.getRandomString("", 4, 8)과 같은 자리수대의 임시 비밀번호. */
    private static String generateTempPassword() {
        String chars = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private static String now() {
        return LocalDateTime.now().format(DATETIME_FORMAT);
    }

    private static String today() {
        return LocalDateTime.now().format(DATE_FORMAT);
    }
}
