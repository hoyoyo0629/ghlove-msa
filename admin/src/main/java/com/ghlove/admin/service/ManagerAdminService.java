package com.ghlove.admin.service;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.repository.ManagerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * D2 매니저(admin 로그인계정) 사후 조회/수정/삭제 (AS-IS opmanager/user/manager -
 * UserManagerController, docs/as-is-admin-gap-deep-audit-part2.md 배치D 참고). 이미 있는
 * ManagerRequestController(승인요청 큐)는 신규 계정 발급 절차이고, 이 화면은 이미 발급된
 * OP_MANAGER 행을 관리자가 직접 검색/수정/삭제하고 임시비밀번호를 재발급하는 별개 기능이다.
 */
@Service
@RequiredArgsConstructor
public class ManagerAdminService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_LOCKED = "LOCKED";

    private final ManagerRepository managerRepository;
    private final PasswordEncoder passwordEncoder;

    /** 저트래픽 화면 - OffgiveController와 동일하게 인메모리 필터+페이지네이션. */
    public SearchResult search(String authority, String keyword, int page, int size) {
        List<Manager> all = managerRepository.findAllByOrderByUserIdDesc();
        if (authority != null && !authority.isBlank()) {
            all = all.stream().filter(m -> authority.equals(m.getAuthority())).toList();
        }
        if (keyword != null && !keyword.isBlank()) {
            String v = keyword.trim();
            all = all.stream().filter(m -> (m.getLoginId() != null && m.getLoginId().contains(v))
                    || (m.getUserName() != null && m.getUserName().contains(v))).toList();
        }
        int totalElements = all.size();
        int totalPages = (int) Math.ceil(totalElements / (double) Math.max(size, 1));
        int from = Math.min(Math.max(page, 0) * size, totalElements);
        int to = Math.min(from + size, totalElements);
        return new SearchResult(all.subList(from, to), totalElements, totalPages);
    }

    public Manager get(Long userId) {
        return managerRepository.findById(userId)
                .orElseThrow(() -> new ManagerException("관리자 계정을 찾을 수 없습니다."));
    }

    /** 등록 - 임시비밀번호를 자동 발급한다(ManagerRequestService.approve()와 동일한 정책,
     *  화면에 1회만 노출). */
    @Transactional
    public String create(String loginId, String userName, String email, String authority, String locgovCode) {
        if (loginId == null || loginId.isBlank()) {
            throw new ManagerException("아이디를 입력해 주세요.");
        }
        if (managerRepository.findByLoginId(loginId).isPresent()) {
            throw new ManagerException("이미 사용 중인 아이디입니다.");
        }
        if (authority == null || authority.isBlank()) {
            throw new ManagerException("권한을 선택해 주세요.");
        }
        if (MenuService.LOCGOV_SCOPED_ROLES.contains(authority) && (locgovCode == null || locgovCode.isBlank())) {
            throw new ManagerException("지자체 담당자는 소속 지자체를 선택해 주세요.");
        }
        String tempPassword = generateTempPassword();
        Manager manager = new Manager();
        manager.setLoginId(loginId);
        manager.setPassword(passwordEncoder.encode(tempPassword));
        manager.setUserName(userName);
        manager.setEmail(email);
        manager.setStatusCode(STATUS_ACTIVE);
        manager.setLoginCount(0);
        manager.setLoginFailCount(0);
        manager.setAuthority(authority);
        manager.setLocgovCode(MenuService.LOCGOV_SCOPED_ROLES.contains(authority) ? locgovCode : null);
        manager.setCreatedDate(now());
        manager.setUpdatedDate(now());
        managerRepository.save(manager);
        return tempPassword;
    }

    @Transactional
    public void update(Long userId, String userName, String email, String authority, String locgovCode,
                        String statusCode) {
        Manager manager = get(userId);
        if (authority == null || authority.isBlank()) {
            throw new ManagerException("권한을 선택해 주세요.");
        }
        if (MenuService.LOCGOV_SCOPED_ROLES.contains(authority) && (locgovCode == null || locgovCode.isBlank())) {
            throw new ManagerException("지자체 담당자는 소속 지자체를 선택해 주세요.");
        }
        manager.setUserName(userName);
        manager.setEmail(email);
        manager.setAuthority(authority);
        manager.setLocgovCode(MenuService.LOCGOV_SCOPED_ROLES.contains(authority) ? locgovCode : null);
        if (statusCode != null && !statusCode.isBlank()) {
            manager.setStatusCode(statusCode);
            if (STATUS_ACTIVE.equals(statusCode)) {
                manager.setLoginFailCount(0);
            }
        }
        manager.setUpdatedDate(now());
        managerRepository.save(manager);
    }

    @Transactional
    public void delete(Long userId, Long currentManagerUserId) {
        if (userId.equals(currentManagerUserId)) {
            throw new ManagerException("본인 계정은 삭제할 수 없습니다.");
        }
        if (!managerRepository.existsById(userId)) {
            throw new ManagerException("관리자 계정을 찾을 수 없습니다.");
        }
        managerRepository.deleteById(userId);
    }

    /** 임시비밀번호 재발급 - 계정 잠금(로그인 실패 초과)도 함께 풀어준다. */
    @Transactional
    public String resetPassword(Long userId) {
        Manager manager = get(userId);
        String tempPassword = generateTempPassword();
        manager.setPassword(passwordEncoder.encode(tempPassword));
        manager.setLoginFailCount(0);
        if (STATUS_LOCKED.equals(manager.getStatusCode())) {
            manager.setStatusCode(STATUS_ACTIVE);
        }
        manager.setUpdatedDate(now());
        managerRepository.save(manager);
        return tempPassword;
    }

    private static String generateTempPassword() {
        String chars = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$%";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private static String now() {
        return LocalDateTime.now().format(DATE_FORMAT);
    }

    public record SearchResult(List<Manager> content, long totalElements, int totalPages) {
    }
}
