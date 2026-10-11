package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.domain.Role;
import com.ghlove.admin.repository.ManagerRepository;
import com.ghlove.admin.repository.RoleRepository;
import com.ghlove.admin.service.CommonCodeService;
import com.ghlove.admin.service.MemberAdminClient;
import com.ghlove.admin.web.support.Pagination;
import com.ghlove.admin.web.support.SecedeUserSearchParam;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 회원탈퇴관리 (메뉴 4105) - AS-IS saleson.shop.user.SecedeUserManagerController
 * ({@code /opmanager/user/secede-user}) 재현.
 *
 * <p><b>AS-IS 핵심 동작: GET(진입)에서는 목록을 조회하지 않는다</b> - 컨트롤러가
 * {@code Collections.EMPTY_LIST} / count 0을 그대로 내려주고, 검색(POST)해야 실제로 조회된다
 * (화면에도 "검색 버튼을 클릭하면 조건에 맞는 검색결과를 확인할 수 있습니다." 안내가 있다).
 * 탈퇴일 기본값은 오늘이다. 예전 TO-BE는 GET에서 바로 전체를 조회하고 탈퇴구분·검색구분이 없었다.
 *
 * <p>화면 6컬럼은 AS-IS 쿼리를 서비스 경계에 맞춰 나눠 채운다:
 * <ul>
 *   <li>탈퇴일·아이디·탈퇴사유·탈퇴코드·<b>탈퇴처리자ID</b> → member({@code MemberAdminClient.searchSecede})</li>
 *   <li>탈퇴사유 라벨 → admin의 공통코드 {@code LEAVE_CODE}</li>
 *   <li>담당자(권한그룹 + 이름) → admin의 {@code OP_MANAGER}/{@code OP_ROLE}에서 탈퇴처리자ID로 찾는다
 *       (AS-IS는 OP_MANAGER를 먼저 보고 없으면 OP_USER로 폴백하는데, 탈퇴 처리자는 관리자이므로
 *       OP_MANAGER 경로만 쓴다 - OP_USER 폴백은 member 조회가 더 필요해 미이식)</li>
 * </ul>
 */
@Controller
@RequiredArgsConstructor
public class SecedeUserAdminController {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** AS-IS 탈퇴사유 코드 타입. */
    private static final String CODE_TYPE_LEAVE = "LEAVE_CODE";

    /** AS-IS 탈퇴구분 - 탈퇴처리자가 있으면 관리자탈퇴(M), 없으면 회원탈퇴(U). */
    private static final String LEAVE_TYPE_BY_MANAGER = "M";
    private static final String LEAVE_TYPE_BY_USER = "U";

    private final MemberAdminClient memberAdminClient;
    private final CommonCodeService commonCodeService;
    private final ManagerRepository managerRepository;
    private final RoleRepository roleRepository;

    /** 화면 한 줄 - AS-IS SecedeUser 도메인에 대응. */
    public record Row(Long userId, String leaveDate, String loginId, String leaveReason, String leaveCodeLabel,
                      Long leaveUserId, String leaveUserName, String roleName) {

        /** AS-IS: 탈퇴처리자가 있으면 M(관리자탈퇴). */
        public String getLeaveType() {
            return leaveUserId != null ? LEAVE_TYPE_BY_MANAGER : LEAVE_TYPE_BY_USER;
        }

        /** AS-IS 목록/팝업의 탈퇴사유 = 코드라벨 + ' / ' + 직접입력 사유(둘 중 하나만 있으면 그것만). */
        public String getLeaveReasonText() {
            boolean hasLabel = leaveCodeLabel != null && !leaveCodeLabel.isBlank();
            boolean hasReason = leaveReason != null && !leaveReason.isBlank();
            if (hasLabel && hasReason) {
                return leaveCodeLabel + " / " + leaveReason;
            }
            if (hasLabel) {
                return leaveCodeLabel;
            }
            return hasReason ? leaveReason : "";
        }
    }

    /** AS-IS GET /list - 조회하지 않고 빈 목록을 보여준다. */
    @GetMapping("/admin/secede-users")
    public String list(@ModelAttribute("searchParam") SecedeUserSearchParam searchParam,
                       HttpServletRequest request, Model model) {
        applyDefaults(searchParam);
        model.addAttribute("list", List.of());
        model.addAttribute("count", 0);
        model.addAttribute("pagination",
                Pagination.of(0, searchParam.getPage(), searchParam.getItemsPerPage()).withLinkFrom(request));
        return "secede-user-admin/list";
    }

    /** AS-IS POST /list - 실제 검색. */
    @PostMapping("/admin/secede-users")
    public String search(@ModelAttribute("searchParam") SecedeUserSearchParam searchParam,
                         HttpServletRequest request, Model model) {
        applyDefaults(searchParam);

        List<Row> all = rowsOf(searchParam);
        Pagination pagination = Pagination.of(all.size(), searchParam.getPage(), searchParam.getItemsPerPage())
                .withLinkFrom(request);
        model.addAttribute("list", all.stream()
                .skip(pagination.getStartRow())
                .limit(pagination.getItemsPerPage())
                .toList());
        model.addAttribute("count", all.size());
        model.addAttribute("pagination", pagination);
        return "secede-user-admin/list";
    }

    /** AS-IS /popup/reason-details/{userId} - 탈퇴사유 팝업(600x350). */
    @GetMapping("/admin/secede-users/popup/reason-details/{userId}")
    public String reasonDetailsPopup(@PathVariable Long userId, Model model) {
        // 기간을 좁히면 대상이 빠질 수 있어 전 구간에서 그 회원만 찾는다
        SecedeUserSearchParam all = new SecedeUserSearchParam();
        all.setSrchKey(null);
        all.setSrchValue(null);
        model.addAttribute("details", rowsOf(all).stream()
                .filter(r -> userId.equals(r.userId()))
                .findFirst()
                .orElse(null));
        return "user-popup/secede-reason-details";
    }

    /**
     * member의 탈퇴회원 조회 결과에 admin 쪽 라벨·담당자를 채워 AS-IS 한 줄로 만든다.
     * member API에 탈퇴구분 조건이 없으므로 그 필터만 admin에서 적용한다(AS-IS와 결과는 동일).
     */
    private List<Row> rowsOf(SecedeUserSearchParam p) {
        Map<String, String> leaveLabels = commonCodeService.labelsOf(CODE_TYPE_LEAVE);
        Map<String, String> roleNames = roleRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(Role::getAuthority, Role::getRoleName, (a, b) -> a));

        // member의 회원검색 API는 page가 0부터이고 페이징은 admin에서 다시 한다(AS-IS 탈퇴구분
        // 필터가 member에 없어 전량을 받아 걸러야 하기 때문)
        return memberAdminClient.searchSecede(p.getSrchStartLeaveDateForApi(), p.getSrchEndLeaveDateForApi(),
                        blankToNull(p.getSrchKey()), blankToNull(p.getSrchValue()), 0, 1000)
                .content().stream()
                .filter(r -> matchesLeaveType(p.getSrchLeaveType(), r.leaveUserId()))
                .map(r -> {
                    Manager handler = r.leaveUserId() == null ? null
                            : managerRepository.findById(r.leaveUserId()).orElse(null);
                    return new Row(r.userId(), r.leaveDate(), r.loginId(), r.leaveReason(),
                            leaveLabels.get(r.leaveCode()), r.leaveUserId(),
                            handler == null ? null : handler.getUserName(),
                            handler == null ? null : roleNames.get(handler.getAuthority()));
                })
                .toList();
    }

    /** AS-IS: U면 탈퇴처리자가 없는 건, M이면 있는 건. 빈 값이면 전체. */
    private static boolean matchesLeaveType(String srchLeaveType, Long leaveUserId) {
        if (srchLeaveType == null || srchLeaveType.isBlank()) {
            return true;
        }
        return LEAVE_TYPE_BY_MANAGER.equals(srchLeaveType) ? leaveUserId != null : leaveUserId == null;
    }

    /** AS-IS 컨트롤러 - 탈퇴일이 비면 오늘로 채운다. */
    private static void applyDefaults(SecedeUserSearchParam p) {
        String today = LocalDate.now().format(DAY);
        if (p.getSrchStartLeaveDate() == null || p.getSrchStartLeaveDate().isBlank()) {
            p.setSrchStartLeaveDate(today);
        }
        if (p.getSrchEndLeaveDate() == null || p.getSrchEndLeaveDate().isBlank()) {
            p.setSrchEndLeaveDate(today);
        }
        if (p.getItemsPerPage() <= 0) {
            p.setItemsPerPage(Pagination.DEFAULT_ITEMS_PER_PAGE);
        }
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
