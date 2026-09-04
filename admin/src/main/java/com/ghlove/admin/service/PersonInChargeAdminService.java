package com.ghlove.admin.service;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.repository.ManagerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

/**
 * D6/D9 관리자 사후관리 - 지자체담당자(ROLE_ADMIN_5/6, LocgovPersonInChargeManagerController)와
 * 운영담당자(ROLE_ADMIN_1~4, OperPersonInChargeManagerController)를 하나의 화면(탭)으로 통합
 * 구현한다(docs/as-is-admin-gap-deep-audit-part2.md D6/D9 권장사항). 이미 있는
 * {@link ManagerAdminService}(범용 계정 CRUD, 등록 포함)와 별개로, 이 서비스는 AS-IS의
 * 특수 업무규칙(주담당자 인원제한, 중지상태 자동강등, 삭제 허용권한 제한)만 다룬다 - 신규
 * 계정 등록은 여전히 {@link ManagerRequestController}(승인요청 큐) 또는 ManagerAdminController를
 * 통해서만 이뤄진다.
 */
@Service
@RequiredArgsConstructor
public class PersonInChargeAdminService {

    public static final String SCOPE_LOCGOV = "locgov";
    public static final String SCOPE_OPERATOR = "operator";

    /** 지자체 주담당자 - 지자체당 최대 2명. */
    private static final String LOCGOV_MAIN = "ROLE_ADMIN_5";
    private static final String LOCGOV_SUB = "ROLE_ADMIN_6";
    private static final int LOCGOV_MAIN_LIMIT = 2;

    /** 행안부 주담당자 - 전체(단일 조직) 최대 2명. */
    private static final String OPERATOR_MAIN = "ROLE_ADMIN_3";
    private static final int OPERATOR_MAIN_LIMIT = 2;

    /** 지자체담당자 삭제 허용권한: 1~5(6은 불가). */
    private static final Set<String> LOCGOV_DELETE_ALLOWED = Set.of(
            "ROLE_ADMIN_1", "ROLE_ADMIN_2", "ROLE_ADMIN_3", "ROLE_ADMIN_4", "ROLE_ADMIN_5");
    /** 운영담당자 삭제 허용권한: 1,3만. */
    private static final Set<String> OPERATOR_DELETE_ALLOWED = Set.of("ROLE_ADMIN_1", "ROLE_ADMIN_3");

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ManagerRepository managerRepository;

    /** 지자체담당자/운영담당자 목록 - 등록일(createdDate) 범위검색 + 지자체 스코프. */
    public SearchResult search(String scope, String fromDate, String toDate, Manager viewer, int page, int size) {
        Set<String> scopeAuthorities = SCOPE_LOCGOV.equals(scope) ? Set.of(LOCGOV_MAIN, LOCGOV_SUB)
                : Set.of("ROLE_ADMIN_1", "ROLE_ADMIN_2", OPERATOR_MAIN, "ROLE_ADMIN_4");

        List<Manager> all = managerRepository.findAllByOrderByUserIdDesc().stream()
                .filter(m -> scopeAuthorities.contains(m.getAuthority()))
                .toList();

        String from = rangeStart(fromDate);
        String to = rangeEnd(toDate);
        all = all.stream()
                .filter(m -> m.getCreatedDate() != null && m.getCreatedDate().compareTo(from) >= 0
                        && m.getCreatedDate().compareTo(to) <= 0)
                .toList();

        // 지자체담당자(5/6)가 조회하는 경우, 탭과 무관하게 항상 자기 지자체로 강제 스코프한다.
        if (MenuService.isLocgovScoped(viewer)) {
            all = all.stream().filter(m -> viewer.getLocgovCode() != null
                    && viewer.getLocgovCode().equals(m.getLocgovCode())).toList();
        }

        int totalElements = all.size();
        int totalPages = (int) Math.ceil(totalElements / (double) Math.max(size, 1));
        int start = Math.min(Math.max(page, 0) * size, totalElements);
        int end = Math.min(start + size, totalElements);
        return new SearchResult(all.subList(start, end), totalElements, totalPages);
    }

    public Manager get(Long userId) {
        return managerRepository.findById(userId)
                .orElseThrow(() -> new ManagerException("계정을 찾을 수 없습니다."));
    }

    /** 수정 - 주담당자 인원제한 + "주담당자+중지상태" 자동 부담당자 강등. */
    @Transactional
    public void update(String scope, Long userId, String userName, String email, String authority,
                        String statusCode) {
        Manager manager = get(userId);
        if (authority == null || authority.isBlank()) {
            throw new ManagerException("권한을 선택해 주세요.");
        }

        if (SCOPE_LOCGOV.equals(scope)) {
            if (LOCGOV_MAIN.equals(authority) && !LOCGOV_MAIN.equals(manager.getAuthority())) {
                enforceMainLimit(manager.getLocgovCode(), userId, LOCGOV_MAIN, LOCGOV_MAIN_LIMIT,
                        "이 지자체의 주담당자는 최대 " + LOCGOV_MAIN_LIMIT + "명까지 지정할 수 있습니다.");
            }
        } else {
            if (OPERATOR_MAIN.equals(authority) && !OPERATOR_MAIN.equals(manager.getAuthority())) {
                enforceMainLimit(null, userId, OPERATOR_MAIN, OPERATOR_MAIN_LIMIT,
                        "행안부 주담당자는 전체 최대 " + OPERATOR_MAIN_LIMIT + "명까지 지정할 수 있습니다.");
            }
        }

        manager.setUserName(userName);
        manager.setEmail(email);
        manager.setAuthority(authority);
        if (statusCode != null && !statusCode.isBlank()) {
            manager.setStatusCode(statusCode);
        }

        // "주담당자 + 중지상태"면 자동으로 부담당자로 강등 (AS-IS 업무규칙).
        if (SCOPE_LOCGOV.equals(scope) && LOCGOV_MAIN.equals(manager.getAuthority())
                && ManagerAdminService.STATUS_LOCKED.equals(manager.getStatusCode())) {
            manager.setAuthority(LOCGOV_SUB);
        }

        manager.setUpdatedDate(now());
        managerRepository.save(manager);
    }

    private void enforceMainLimit(String locgovCode, Long excludeUserId, String mainAuthority, int limit,
                                   String errorMessage) {
        long count = managerRepository.findAllByOrderByUserIdDesc().stream()
                .filter(m -> mainAuthority.equals(m.getAuthority()))
                .filter(m -> !m.getUserId().equals(excludeUserId))
                .filter(m -> locgovCode == null || locgovCode.equals(m.getLocgovCode()))
                .count();
        if (count >= limit) {
            throw new ManagerException(errorMessage);
        }
    }

    /** 삭제 - 스코프별 허용권한 확인. 본인 삭제는 허용하되(세션무효화는 컨트롤러가 처리),
     *  타인 삭제는 허용권한 목록에 있는 관리자만 가능하다. */
    @Transactional
    public void delete(String scope, Long userId, Manager viewer) {
        Set<String> allowed = SCOPE_LOCGOV.equals(scope) ? LOCGOV_DELETE_ALLOWED : OPERATOR_DELETE_ALLOWED;
        if (!allowed.contains(viewer.getAuthority())) {
            throw new ManagerException("이 계정을 삭제할 권한이 없습니다.");
        }
        if (!managerRepository.existsById(userId)) {
            throw new ManagerException("계정을 찾을 수 없습니다.");
        }
        managerRepository.deleteById(userId);
    }

    private static String rangeStart(String dateStr) {
        return parseOrDefault(dateStr, LocalDate.now().minusYears(50)).format(DateTimeFormatter.BASIC_ISO_DATE) + "000000";
    }

    private static String rangeEnd(String dateStr) {
        return parseOrDefault(dateStr, LocalDate.now()).format(DateTimeFormatter.BASIC_ISO_DATE) + "235959";
    }

    private static LocalDate parseOrDefault(String dateStr, LocalDate fallback) {
        if (dateStr == null || dateStr.isBlank()) {
            return fallback;
        }
        try {
            return LocalDate.parse(dateStr);
        } catch (RuntimeException e) {
            return fallback;
        }
    }

    private static String now() {
        return LocalDateTime.now().format(DATE_FORMAT);
    }

    public record SearchResult(List<Manager> content, long totalElements, int totalPages) {
    }
}
